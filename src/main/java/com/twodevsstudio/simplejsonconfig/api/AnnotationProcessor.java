package com.twodevsstudio.simplejsonconfig.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.twodevsstudio.simplejsonconfig.SimpleJSONConfig;
import com.twodevsstudio.simplejsonconfig.data.Identifiable;
import com.twodevsstudio.simplejsonconfig.data.Stored;
import com.twodevsstudio.simplejsonconfig.data.cache.InMemoryCache;
import com.twodevsstudio.simplejsonconfig.data.service.FileService;
import com.twodevsstudio.simplejsonconfig.def.Serializer;
import com.twodevsstudio.simplejsonconfig.def.StoreType;
import com.twodevsstudio.simplejsonconfig.def.adapters.FieldValidator;
import com.twodevsstudio.simplejsonconfig.exceptions.ConfigDeprecatedException;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;
import com.twodevsstudio.simplejsonconfig.interfaces.Comment;
import com.twodevsstudio.simplejsonconfig.interfaces.Configuration;
import com.twodevsstudio.simplejsonconfig.utils.CustomLogger;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.isStatic;
import static org.reflections.scanners.Scanners.*;

public class AnnotationProcessor {

    public static Map<String, Comment> getFieldsComments(Object object) {

        Map<String, Comment> comments = new HashMap<>();

        for (Field declaredField : object.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            if (!declaredField.isAnnotationPresent(Comment.class)) {
                continue;
            }

            Comment comment = declaredField.getAnnotation(Comment.class);
            comments.put(declaredField.getName(), comment);
        }

        return comments;
    }

    public void processAnnotations(@NotNull Plugin plugin, File configsDirectory, Set<Plugin> dependencies) {

        Reflections reflections = buildReflections(plugin.getClass().getPackage().getName(),
                getClassLoaders(dependencies, plugin.getClass().getClassLoader(), ClassLoader.getSystemClassLoader(),
                        ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader()
                )
        );

        processConfiguration(configsDirectory, reflections);
        processStores(plugin.getDataFolder().toPath(), reflections);
    }

    public void processConfiguration(File configsDirectory, Class<?> clazz, Set<Plugin> dependencies) {

        Reflections reflections = buildReflections(clazz.getPackage().getName(),
                getClassLoaders(dependencies, clazz.getClassLoader(), ClassLoader.getSystemClassLoader(),
                        ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader()
                )
        );

        processConfiguration(configsDirectory, reflections);
    }

    @SneakyThrows
    public void processConfiguration(File configsDirectory, Reflections reflections) {

        Set<Class<?>> configurationClasses = reflections.get(TypesAnnotated.with(Configuration.class).asClass());

        for (Class<?> annotatedClass : configurationClasses) {

            Configuration configurationAnnotation = annotatedClass.getAnnotation(Configuration.class);
            String configName = configurationAnnotation.value();

            if (!isConfig(annotatedClass)) {
                CustomLogger.warning("Configuration " +
                        configName +
                        " could not be loaded. Class annotated as @Configuration does not extends " +
                        Config.class.getName());

                continue;
            }

            Class<? extends Config> configClass = (Class<? extends Config>) annotatedClass;

            Constructor<? extends Config> constructor;
            Config config;

            try {
                constructor = configClass.getConstructor();
                constructor.setAccessible(true);
                config = constructor.newInstance();
            } catch (ReflectiveOperationException exception) {
                CustomLogger.warning(configClass.getName() + ": " + exception.getMessage());
                exception.printStackTrace();
                continue;
            }

            StoreType configType = Config.getType();
            String fileName = configName.endsWith(configType.getExtension()) ?
                    configName :
                    configName + configType.getExtension();

            File configFile = new File(configsDirectory, fileName);

            Field field = configClass.getSuperclass().getDeclaredField("configFile");
            field.setAccessible(true);
            field.set(config, configFile);

            initConfig(config, configFile, configurationAnnotation, true);

        }

    }

    public void processStores(Path pluginDirectory, Class<?> clazz, Set<Plugin> dependencies) {

        Reflections reflections = buildReflections(clazz.getPackage().getName(),
                getClassLoaders(dependencies, clazz.getClassLoader(), ClassLoader.getSystemClassLoader(),
                        ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader()
                )
        );

        processStores(pluginDirectory, reflections);
    }

    @SneakyThrows
    public void processStores(Path pluginDirectory, Reflections reflections) {

        Set<Class<?>> storedClasses = reflections.get(TypesAnnotated.with(Stored.class).asClass());

        for (Class<?> storedClass : storedClasses) {
            if (!isStored(storedClass)) {
                CustomLogger.warning("Cannot create a Service for " +
                        storedClass.getName() +
                        ". Class annotated as @Stored does not implement " +
                        Identifiable.class.getName());

                continue;
            }

            Class<Identifiable> identifiable = (Class<Identifiable>) storedClass;

            Stored annotation = storedClass.getAnnotation(Stored.class);
            String storeDirectoryPath = annotation.value();
            StoreType storeType = annotation.storeType();

            Path path = Paths.get(pluginDirectory.toString(), storeDirectoryPath);
            Files.createDirectories(path);

            InMemoryCache cache = new InMemoryCache(annotation.cacheLifespanSeconds(),
                    annotation.cacheScanIntervalSeconds(), annotation.cacheMaxSize()
            );

            Service service = new FileService<>(identifiable, path, storeType, cache);

            ServiceContainer.SINGLETONS.put(identifiable, service);
        }
    }

    private ClassLoader[] getClassLoaders(Set<Plugin> dependencies, ClassLoader... additionalClassLoaders) {

        List<ClassLoader> classLoaders = dependencies.stream()
                .map(dependency -> dependency.getClass().getClassLoader())
                .collect(Collectors.toList());

        classLoaders.addAll(Arrays.asList(additionalClassLoaders));

        return classLoaders.toArray(new ClassLoader[0]);
    }

    @SneakyThrows
    public void processAutowired(Set<Plugin> dependencies) {

        ConfigurationBuilder builder = new ConfigurationBuilder();

        ClassLoader[] classLoaders = getClassLoaders(dependencies, ClassLoader.getSystemClassLoader(),
                ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader()
        );

        List<URL> urls = new ArrayList<>();

        for (Plugin dependency : dependencies) {
            urls.addAll(ClasspathHelper.forPackage(dependency.getClass().getPackage().getName(), classLoaders));
        }

        builder.addUrls(urls);
        builder.addScanners(TypesAnnotated, FieldsAnnotated, SubTypes);

        Reflections reflections = new Reflections(builder);

        processAutowired(reflections);
    }

    @SneakyThrows
    public void processAutowired(Reflections reflections) {

        for (Field field : reflections.get(FieldsAnnotated.with(Autowired.class).as(Field.class))) {

            field.setAccessible(true);

            if (SimpleJSONConfig.INSTANCE.isEnableDebug()) {
                CustomLogger.log("Autowiring config class to field : " +
                        field.getDeclaringClass().getTypeName() +
                        "." +
                        field.getName());
            }

            Class<?> type = field.getType();

            if (isConfig(type) && isStatic(field.getModifiers()) && field.get(null) == null) {

                Config config = Config.getConfig((Class<? extends Config>) type);
                field.set(null, config);
            }
            if (isStored(type) && isStatic(field.getModifiers()) && field.get(null) == null) {

                Service service = Service.getService((Class<? extends Identifiable>) type);
                field.set(null, service);
            }

        }
    }

    public boolean isConfig(@NotNull Class<?> clazz) {

        return clazz.getSuperclass() == Config.class;
    }

    public boolean isStored(@NotNull Class<?> clazz) {

        return Identifiable.class.isAssignableFrom(clazz);
    }

    private void initConfig(@NotNull Config config,
                            @NotNull File configFile,
                            @NotNull Configuration annotation,
                            boolean runValidation
    ) {

        config.configFile = configFile;

        Serializer serializer = Serializer.getInst();
        if (!configFile.exists()) {

            try {
                configFile.mkdirs();
                configFile.createNewFile();
                serializer.saveConfig(config, configFile, Config.getType(), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }

        } else {

            if (runValidation) {
                serializer.toBuilder().registerTypeHierarchyAdapter(Config.class, new FieldValidator()).build();
            }

            try {
                config.reload();
            } catch (ConfigDeprecatedException exception) {
                serializer.toBuilder().unregisterTypeHierarchyAdapter(Config.class, FieldValidator.class).build();

                if (!annotation.enableConfigAutoUpdates()) {
                    CustomLogger.warning(exception.getMessage());
                    config.reload();
                } else {
                    handleOutdatedConfigUpdate(config, configFile, annotation, exception);
                    return;
                }

            } catch (UnsupportedOperationException ignored) {
                serializer.toBuilder().unregisterTypeHierarchyAdapter(Config.class, FieldValidator.class).build();
                config.reload();
            } catch (Exception exception) {
                CustomLogger.warning(config.getClass().getName() + ": Config file is corrupted");
                exception.printStackTrace();
                return;
            }


        }

        ConfigContainer.SINGLETONS.put(config.getClass(), config);
    }

    @SneakyThrows
    private void handleOutdatedConfigUpdate(@NotNull Config config,
                                            @NotNull File configFile,
                                            @NotNull Configuration annotation,
                                            ConfigDeprecatedException exception
    ) {

        JsonObject sourceJson = exception.getSourceJson().getAsJsonObject();
        exception.getRedundantFields().forEach(sourceJson::remove);

        Class<? extends @NotNull Config> configClass = config.getClass();

        List<String> missingFields = exception.getMissingFields();

        Serializer serializer = Serializer.getInst();
        for (Field declaredField : configClass.getDeclaredFields()) {

            declaredField.setAccessible(true);
            if (!missingFields.contains(declaredField.getName())) {
                continue;
            }

            Object fieldValue = declaredField.get(config);
            JsonElement jsonElement = serializer.getGson().toJsonTree(fieldValue);
            sourceJson.add(declaredField.getName(), jsonElement);

        }

        Config mergedConfig = serializer.getGson().fromJson(sourceJson, configClass);

        Field configFileField = mergedConfig.getClass().getSuperclass().getDeclaredField("configFile");
        configFileField.setAccessible(true);
        configFileField.set(mergedConfig, configFile);

        mergedConfig.save();
        initConfig(config, configFile, annotation, false);
        CustomLogger.log(String.format(
                "Config \"%s\" has been updated! %nMissing fields added: %s %nRedundant fields removed: %s",
                configFile.getName(), exception.getMissingFields(), exception.getRedundantFields()
        ));
    }

    private Reflections buildReflections(String packageName, ClassLoader[] classLoaders) {

        ConfigurationBuilder builder = new ConfigurationBuilder();

        builder.addUrls(ClasspathHelper.forPackage(packageName, classLoaders));

        builder.addScanners(TypesAnnotated, FieldsAnnotated, SubTypes);

        return new Reflections(builder);
    }
}
