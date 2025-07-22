package com.twodevsstudio.simplejsonconfig.def;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SharedGsonBuilder {

    private final Serializer targetSerializer;

    private final Map<Class<?>, List<Object>> typeHierarchyAdapters = new ConcurrentHashMap<>();
    private final Map<Type, List<Object>> typeAdapters = new ConcurrentHashMap<>();
    private final List<ExclusionStrategy> deserializationExclusionStrategies = new ArrayList<>();
    private final List<ExclusionStrategy> serializationExclusionStrategies = new ArrayList<>();

    public SharedGsonBuilder registerTypeHierarchyAdapter(Class<?> baseType, Object typeAdapter) {

        typeHierarchyAdapters.putIfAbsent(baseType, new ArrayList<>());
        List<Object> adapters = typeHierarchyAdapters.get(baseType);
        if (adapters.contains(typeAdapter)) {
            return this;
        }

        adapters.add(typeAdapter);
        return this;
    }

    public SharedGsonBuilder unregisterTypeHierarchyAdapter(Class<?> baseType, Class<?> typeAdapterClass) {

        if (!typeHierarchyAdapters.containsKey(baseType)) {
            return this;
        }

        typeHierarchyAdapters.get(baseType).removeIf(typeAdapter -> typeAdapter.getClass().equals(typeAdapterClass));
        return this;
    }

    public SharedGsonBuilder registerTypeAdapter(Type type, Object typeAdapter) {

        typeAdapters.putIfAbsent(type, new ArrayList<>());
        List<Object> adapters = typeAdapters.get(type);
        if (adapters.contains(typeAdapter)) {
            return this;
        }

        adapters.add(typeAdapter);
        return this;
    }

    public SharedGsonBuilder unregisterTypeAdapter(Type type, Object typeAdapterClass) {

        if (!typeAdapters.containsKey(type)) {
            return this;
        }

        typeAdapters.get(type).removeIf(typeAdapter -> typeAdapter.getClass().equals(typeAdapterClass));
        return this;
    }

    public SharedGsonBuilder addDeserializationExclusionStrategy(ExclusionStrategy strategy) {

        if (deserializationExclusionStrategies.contains(strategy)) {
            return this;
        }
        deserializationExclusionStrategies.add(strategy);
        return this;
    }

    public SharedGsonBuilder removeDeserializationExclusionStrategy(Class<ExclusionStrategy> strategyClass) {

        deserializationExclusionStrategies.removeIf(strategy -> strategy.getClass().equals(strategyClass));
        return this;
    }

    public SharedGsonBuilder addSerializationExclusionStrategy(ExclusionStrategy strategy) {

        if (serializationExclusionStrategies.contains(strategy)) {
            return this;
        }
        serializationExclusionStrategies.add(strategy);
        return this;
    }

    public SharedGsonBuilder removeSerializationExclusionStrategy(Class<ExclusionStrategy> strategyClass) {

        serializationExclusionStrategies.removeIf(strategy -> strategy.getClass().equals(strategyClass));
        return this;
    }

    public void build() {

        targetSerializer.setGson(buildGson());
    }

    public Gson buildGson() {

        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting()
                .disableHtmlEscaping()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
                .serializeNulls();

        for (Map.Entry<Class<?>, List<Object>> typeHierarchyAdapterEntry : typeHierarchyAdapters.entrySet()) {
            Class<?> type = typeHierarchyAdapterEntry.getKey();
            List<Object> adapters = typeHierarchyAdapterEntry.getValue();
            for (Object adapter : adapters) {
                gsonBuilder.registerTypeHierarchyAdapter(type, adapter);
            }
        }

        for (Map.Entry<Type, List<Object>> typeAdapterEntry : typeAdapters.entrySet()) {
            Type type = typeAdapterEntry.getKey();
            List<Object> adapters = typeAdapterEntry.getValue();
            for (Object adapter : adapters) {
                gsonBuilder.registerTypeAdapter(type, adapter);
            }
        }

        deserializationExclusionStrategies.forEach(gsonBuilder::addDeserializationExclusionStrategy);
        serializationExclusionStrategies.forEach(gsonBuilder::addSerializationExclusionStrategy);

        return gsonBuilder.create();
    }
}
