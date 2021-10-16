package com.twodevsstudio.simplejsonconfig.api;

import com.twodevsstudio.simplejsonconfig.data.Identifiable;

import java.util.Collection;
import java.util.function.Predicate;

public interface Service<ID, T extends Identifiable<ID>> {
    
    void save(T object);
    
    void saveAll();
    
    T getById(ID id);
    
    Collection<T> loadAndGetAll();
    
    Collection<T> getMatching(Predicate<T> predicate);
    
    void deleteById(ID id);
    
    void delete(T object);
    
    Collection<T> getAllCached();
    
    void addToCache(T object);
    
    void deleteFromCache(ID id);
    
    static <ID, T extends Identifiable<ID>> Service<ID, T> getService(Class<T> storedType) {
        
        return ServiceContainer.getService(storedType);
    }
}
