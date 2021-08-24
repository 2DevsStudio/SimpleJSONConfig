package com.twodevsstudio.simplejsonconfig;

import com.twodevsstudio.simplejsonconfig.api.AnnotationProcessor;
import com.twodevsstudio.simplejsonconfig.api.Config;
import com.twodevsstudio.simplejsonconfig.def.ConfigType;
import com.twodevsstudio.simplejsonconfig.exceptions.InstanceOverrideException;
import com.twodevsstudio.simplejsonconfig.utils.CustomLogger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum SimpleJSONConfig {
    INSTANCE;
    
    private final AnnotationProcessor annotationProcessor = new AnnotationProcessor();
    private final Map<Plugin, File> plugins = new HashMap<>();
    
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
    
    public void register(JavaPlugin javaPlugin, ConfigType configType) {
        
        register(javaPlugin, new File(javaPlugin.getDataFolder() + "/configuration"), configType);
    }
    
    public void register(JavaPlugin javaPlugin, File configsDirectory, ConfigType configType) {
        
        Config.setType(configType);
        register(javaPlugin, configsDirectory);
    }
}
