package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.*;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;

/**
 * JSON adapter for Kyori Adventure Components
 * Uses Adventure's own GsonComponentSerializer for proper serialization/deserialization
 */
public class ComponentAdapter implements JsonSerializer<Object>, JsonDeserializer<Object> {

    private static final String COMPONENT_CLASS_NAME = "net.kyori.adventure.text.Component";
    private static final String GSON_SERIALIZER_CLASS_NAME = "net.kyori.adventure.text.serializer.gson.GsonComponentSerializer";
    
    private final Object gsonComponentSerializer;
    private final java.lang.reflect.Method serializeMethod;
    private final java.lang.reflect.Method deserializeMethod;

    public ComponentAdapter() {
        try {
            // Get the GsonComponentSerializer instance
            Class<?> serializerClass = Class.forName(GSON_SERIALIZER_CLASS_NAME);
            java.lang.reflect.Method gsonMethod = serializerClass.getMethod("gson");
            this.gsonComponentSerializer = gsonMethod.invoke(null);
            
            // Get the serialize and deserialize methods
            Class<?> componentClass = Class.forName(COMPONENT_CLASS_NAME);
            this.serializeMethod = serializerClass.getMethod("serialize", componentClass);
            this.deserializeMethod = serializerClass.getMethod("deserialize", String.class);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ComponentAdapter - Kyori Adventure not properly available", e);
        }
    }

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, @NotNull JsonSerializationContext context) {
        try {
            // Use Adventure's serializer to convert Component to JSON string
            String jsonString = (String) serializeMethod.invoke(gsonComponentSerializer, src);
            // Parse the JSON string into a JsonElement
            return JsonParser.parseString(jsonString);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Kyori Component", e);
        }
    }

    @Override
    public Object deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) 
            throws JsonParseException {
        try {
            // Convert JsonElement to JSON string
            String jsonString = json.toString();
            // Use Adventure's deserializer to convert JSON string back to Component
            return deserializeMethod.invoke(gsonComponentSerializer, jsonString);
        } catch (Exception e) {
            throw new JsonParseException("Failed to deserialize Kyori Component", e);
        }
    }
}