package com.twodevsstudio.simplejsonconfig.api;

import com.twodevsstudio.simplejsonconfig.def.Serializer;
import com.twodevsstudio.simplejsonconfig.def.StoreType;
import com.twodevsstudio.simplejsonconfig.exceptions.ConfigNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Getter
public abstract class Config {
    
    private static final Serializer SERIALIZER = Serializer.getInst();
    @Setter
    @Getter
    private static StoreType type = StoreType.JSON;
    protected transient File configFile;
    
    protected Config() {
    
    }
    
    /**
     * You can perform a dynamic reload for your configuration to apply changes performed manually inside configuration
     * file
     */
    public void reload() {
        
        Config newConfig = SERIALIZER.loadConfig(getClass(), this.configFile, type);
        
        if (newConfig == null) {
            throw new ConfigNotFoundException(this.configFile);
        }
        
        for (Field newField : newConfig.getClass().getDeclaredFields()) {
            newField.setAccessible(true);
            if (newField.getName().equals("configFile")) {
                continue;
            }
            try {
                newField.set(this, newField.get(newConfig));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void save() {
        
        SERIALIZER.saveConfig(this, configFile, type, StandardCharsets.UTF_8);
    }
    
    /**
     * Use this method to get the instance of configuration that applies to parameterized class
     *
     * @param configClass Specify a class from which you want to get a configuration
     * @param <T>         The type of your configuration, it's type of parameterized class
     *
     * @return Instance of the configuration of specified type (no need for cast to concrete types)
     */
    public static <T extends Config> T getConfig(Class<T> configClass) {
        
        return ConfigContainer.getConfiguration(configClass);
    }
    
    /**
     * Reloads all configurations Invokes method {@code reload()} for each particular configuration
     *
     * @return Set of configurations names that has been reloaded
     */
    @NotNull
    public static Set<String> reloadAll() {
        
        Set<String> reloadedConfigs = new HashSet<>();
        
        for (Config config : ConfigContainer.SINGLETONS.values()) {
            config.reload();
            reloadedConfigs.add(config.getConfigFile().getName());
        }
        
        return reloadedConfigs;
        
    }
    
}
