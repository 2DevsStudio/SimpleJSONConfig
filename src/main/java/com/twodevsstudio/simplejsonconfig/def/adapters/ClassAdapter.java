package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.*;
import lombok.SneakyThrows;

import java.lang.reflect.Type;

public class ClassAdapter implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

    @SneakyThrows
    @Override
    public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        return Class.forName(json.getAsString());
    }

    @Override
    public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {

        return context.serialize(src.getName());
    }
}
