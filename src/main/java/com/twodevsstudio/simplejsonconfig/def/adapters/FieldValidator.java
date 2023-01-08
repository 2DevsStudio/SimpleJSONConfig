package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.twodevsstudio.simplejsonconfig.api.Config;
import com.twodevsstudio.simplejsonconfig.exceptions.ConfigDeprecatedException;
import com.twodevsstudio.simplejsonconfig.interfaces.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class FieldValidator implements JsonDeserializer<Config> {
    
    @Override
    public Config deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        
        Class<?> clazz = TypeToken.get(type).getRawType();
        Field[] declaredFields = clazz.getDeclaredFields();
        
        Set<Map.Entry<String, JsonElement>> entries = json.getAsJsonObject().entrySet();
        
        List<String> allKeys = entries.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> allFields = Arrays.stream(declaredFields)
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .filter(field -> !Modifier.isTransient(field.getModifiers()))
                .map(Field::getName)
                .collect(Collectors.toList());
        
        
        List<String> missingFields = new ArrayList<>();
        for (String field : allFields) {
            if (!allKeys.contains(field)) {
                missingFields.add(field);
            }
        }
        
        List<String> redundantFields = new ArrayList<>();
        for (String key : allKeys) {
            if (!allFields.contains(key)) {
                redundantFields.add(key);
            }
        }
        
        if (!missingFields.isEmpty() || !redundantFields.isEmpty()) {
            String configName = clazz.getAnnotation(Configuration.class).value();
            throw new ConfigDeprecatedException(configName, missingFields, redundantFields, json);
        }
        
        throw new UnsupportedOperationException("Config is up to date!");
    }
}

