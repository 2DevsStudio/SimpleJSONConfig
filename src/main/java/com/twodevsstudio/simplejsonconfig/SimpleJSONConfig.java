package com.twodevsstudio.simplejsonconfig;

import com.twodevsstudio.simplejsonconfig.api.AnnotationProcessor;
import com.twodevsstudio.simplejsonconfig.exceptions.InstanceOverrideException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public enum SimpleJSONConfig {
    INSTANCE;
    
    private AnnotationProcessor annotationProcessor = new AnnotationProcessor();
    private Map<Plugin, File> plugins = new HashMap<>();
    
    public void register(JavaPlugin javaPlugin, File configsDirectory) {
        
        if (plugins.containsKey(javaPlugin)) {
            throw new InstanceOverrideException();
        }
        
        plugins.put(javaPlugin, configsDirectory);
        annotationProcessor.processAnnotations(javaPlugin, configsDirectory);
        
        //Update Fields in other plugins
        plugins.keySet().forEach(annotationProcessor::processAutowired);
    }
    
    public void register(JavaPlugin javaPlugin) {
        
        register(javaPlugin, new File(javaPlugin.getDataFolder() + "/configuration"));
    }
    
}
