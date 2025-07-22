package com.twodevsstudio.simplejsonconfig.api;

import com.twodevsstudio.simplejsonconfig.data.Identifiable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceContainer {

    static final Map<Class<? extends Identifiable>, Service> SINGLETONS = new ConcurrentHashMap<>();

    static <ID, T extends Identifiable<ID>> Service<ID, T> getService(Class<T> storedType) {

        if (!SINGLETONS.containsKey(storedType)) {
            return null;
        }

        return (Service<ID, T>) SINGLETONS.get(storedType);
    }

    static List<Service> getAll() {

        return new ArrayList<>(SINGLETONS.values());
    }

    static List<Service> getByClassLoader(ClassLoader classLoader) {

        List<Service> configsByClassLoader = new ArrayList<>();
        for (Map.Entry<Class<? extends Identifiable>, Service> services : SINGLETONS.entrySet()) {
            if (services.getKey().getClassLoader() == classLoader) {
                configsByClassLoader.add(services.getValue());
            }
        }

        return configsByClassLoader;
    }

}
