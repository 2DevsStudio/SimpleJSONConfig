package com.twodevsstudio.simplejsonconfig;

import com.twodevsstudio.simplejsonconfig.api.AnnotationProcessor;
import com.twodevsstudio.simplejsonconfig.api.Config;
import com.twodevsstudio.simplejsonconfig.def.StoreType;
import com.twodevsstudio.simplejsonconfig.exceptions.InstanceOverrideException;
import com.twodevsstudio.simplejsonconfig.utils.CustomLogger;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public enum SimpleJSONConfig {
    INSTANCE;

    @Getter
    private final AnnotationProcessor annotationProcessor = new AnnotationProcessor();
    private final Map<Plugin, File> plugins = new HashMap<>();
    @Getter
    @Setter
    private boolean enableDebug = false;

    public void register(JavaPlugin javaPlugin, File configsDirectory) {

        CustomLogger.log("Loading plugin: " + javaPlugin.getName());

        if (plugins.containsKey(javaPlugin)) {
            throw new InstanceOverrideException();
        }
        plugins.put(javaPlugin, configsDirectory);

        Set<Plugin> plugins = this.plugins.keySet();

        annotationProcessor.processAnnotations(javaPlugin, configsDirectory, plugins);
        annotationProcessor.processAutowired(plugins);
    }

    public void register(JavaPlugin javaPlugin) {

        register(javaPlugin, new File(javaPlugin.getDataFolder() + "/configuration"));
    }

    public void register(JavaPlugin javaPlugin, StoreType configType) {

        register(javaPlugin, new File(javaPlugin.getDataFolder() + "/configuration"), configType);
    }

    public void register(JavaPlugin javaPlugin, File configsDirectory, StoreType configType) {

        Config.setType(configType);
        register(javaPlugin, configsDirectory);
    }

    /**
     * Does not autowire external configs!
     */
    public void scanConfiguration(JavaPlugin plugin, File directory, Class<?> startingPoint) {

        annotationProcessor.processConfiguration(plugin, directory, startingPoint, plugins.keySet());
    }

    /**
     * Does not autowire external stores!
     */
    public void scanStoredTypes(File directory, Class<?> startingPoint) {

        annotationProcessor.processStores(directory.toPath(), startingPoint, plugins.keySet());
    }
}
