package com.twodevsstudio.simplejsonconfig.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
class ConfigContainer {
    
    static final Map<Class<? extends Config>, Config> SINGLETONS = new HashMap<>();
    
    static <T extends Config> T getConfiguration(Class<T> configClass) {
        
        if (!SINGLETONS.containsKey(configClass)) {
            return null;
        }
        
        return (T) SINGLETONS.get(configClass);
    }
    
    static List<Config> getAll() {
        
        return new ArrayList<>(SINGLETONS.values());
    }
    
    static List<Config> getByClassLoader(ClassLoader classLoader) {
        
        List<Config> configsByClassLoader = new ArrayList<>();
        for (Map.Entry<Class<? extends Config>, Config> configs : SINGLETONS.entrySet()) {
            if (configs.getKey().getClassLoader() == classLoader) {
                configsByClassLoader.add(configs.getValue());
            }
        }
        
        return configsByClassLoader;
    }
}
