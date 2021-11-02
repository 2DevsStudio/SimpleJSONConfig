package com.twodevsstudio.simplejsonconfig.api;

import com.twodevsstudio.simplejsonconfig.data.Identifiable;
import com.twodevsstudio.simplejsonconfig.data.Stored;
import com.twodevsstudio.simplejsonconfig.data.service.FileService;
import com.twodevsstudio.simplejsonconfig.def.Serializer;
import com.twodevsstudio.simplejsonconfig.def.StoreType;
import com.twodevsstudio.simplejsonconfig.def.scanner.SkipRecordsAnnotationScanner;
import com.twodevsstudio.simplejsonconfig.interfaces.Autowired;
import com.twodevsstudio.simplejsonconfig.interfaces.Comment;
import com.twodevsstudio.simplejsonconfig.interfaces.Configuration;
import com.twodevsstudio.simplejsonconfig.utils.CustomLogger;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
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

public class AnnotationProcessor {
    
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
        
        Set<Class<?>> configurationClasses = reflections.getTypesAnnotatedWith(Configuration.class);
        
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
            
            initConfig(config, configFile);
            
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
        
        Set<Class<?>> storedClasses = reflections.getTypesAnnotatedWith(Stored.class);
        
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
            
            Service service = new FileService<>(identifiable, path, storeType);
            
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
        builder.addScanners(new TypeAnnotationsScanner(), new SkipRecordsAnnotationScanner(), new SubTypesScanner());
        
        Reflections reflections = new Reflections(builder);
        
        processAutowired(reflections);
    }
    
    /**
     * get all fields annotated with a given annotation <p/>depends on FieldAnnotationsScanner configured
     */
    private Set<Field> getFieldsAnnotatedWithExcludingRecords(Reflections reflections,
                                                              final Class<? extends Annotation> annotation
    ) {
        
        return reflections.getStore()
                .get(SkipRecordsAnnotationScanner.class, annotation.getName())
                .stream()
                .map(annotated -> Utils.getFieldFromString(annotated, reflections.getConfiguration().getClassLoaders()))
                .collect(Collectors.toSet());
    }
    
    @SneakyThrows
    public void processAutowired(Reflections reflections) {
        
        for (Field field : getFieldsAnnotatedWithExcludingRecords(reflections, Autowired.class)) {
            
            field.setAccessible(true);
            
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
    
    private void initConfig(@NotNull Config config, @NotNull File configFile) {
        
        config.configFile = configFile;
        
        if (!configFile.exists()) {
            
            try {
                configFile.mkdirs();
                configFile.createNewFile();
                Serializer.getInst().saveConfig(config, configFile, Config.getType(), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            
        } else {
            
            try {
                config.reload();
            } catch (Exception exception) {
                CustomLogger.warning(config.getClass().getName() + ": Config file is corrupted");
                exception.printStackTrace();
                return;
            }
            
        }
        
        ConfigContainer.SINGLETONS.put(config.getClass(), config);
    }
    
    
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
    
    private Reflections buildReflections(String packageName, ClassLoader[] classLoaders) {
        
        ConfigurationBuilder builder = new ConfigurationBuilder();
        
        builder.addUrls(ClasspathHelper.forPackage(packageName, classLoaders));
        
        builder.addScanners(new TypeAnnotationsScanner(), new SkipRecordsAnnotationScanner(), new SubTypesScanner());
        
        return new Reflections(builder);
    }
}
