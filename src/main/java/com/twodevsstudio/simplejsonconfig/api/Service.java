package com.twodevsstudio.simplejsonconfig.api;

import com.twodevsstudio.simplejsonconfig.data.Identifiable;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * @param <ID> The Type of the identifier of the stored object
 * @param <T>  The type of the object to store
 *
 * @author Slighterr12
 */
public interface Service<ID, T extends Identifiable<ID>> {
    /**
     * Use this method to get the instance of the service that applies to parameterized class
     *
     * @param storedType Specify a class for which you want to get a service
     *
     * @return Instance of the Service for the specified type
     */
    
    static <ID, T extends Identifiable<ID>> Service<ID, T> getService(Class<T> storedType) {
        return ServiceContainer.getService(storedType);
    }
    
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
}
