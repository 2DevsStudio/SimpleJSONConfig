package com.twodevsstudio.simplejsonconfig.data.service;

import com.twodevsstudio.simplejsonconfig.api.Service;
import com.twodevsstudio.simplejsonconfig.data.Identifiable;
import com.twodevsstudio.simplejsonconfig.data.repository.Repository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor( access = AccessLevel.PROTECTED )
class StandardService<ID, T extends Identifiable<ID>> implements Service<ID, T> {
    
    private final Map<ID, T> cache = new ConcurrentHashMap<>();
    private final Repository<ID, T> repository;
    
    @Override
    public void save(T object) {
        
        if (object == null) {
            return;
        }
        
        addToCache(object);
        repository.save(object);
    }
    
    @Override
    public void saveAll() {
        
        cache.values().forEach(this::save);
    }
    
    @Override
    @Nullable
    @Contract( "null -> null" )
    public T getById(ID id) {
        
        if (id == null) {
            return null;
        }
        
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        
        T object = repository.findById(id);
        if (object != null) {
            addToCache(object);
        }
        
        return object;
    }
    
    @Override
    @NotNull
    public Collection<T> loadAndGetAll() {
        
        Collection<T> all = repository.findAll();
        all.forEach(this::addToCache);
        return cache.values();
    }
    
    @Override
    @NotNull
    public Collection<T> getMatching(Predicate<T> predicate) {
        
        if (predicate == null) {
            return new ArrayList<>();
        }
        
        // Data in cache may differ from the original data in the repository, so we need to replace older records
        // from the repository with more up-to-date version from cache.
        
        // Get all matches from the cache. This data is up-to-date
        List<T> cachedMatching = cache.values().stream().filter(predicate).collect(Collectors.toList());
        // Get all matches from the repository. This data may contain older versions of some records, but also
        // contains data not existing in the cache
        List<T> allMatching = repository.findAll().stream().filter(predicate).collect(Collectors.toList());
        // Map cached data to get List of the IDs of all matching records for the next step filtering
        List<ID> cachedMatchingIds = cachedMatching.stream().map(Identifiable::getId).collect(Collectors.toList());
    
        // Filter out all possibly outdated records.
        List<T> finalMatching = allMatching.stream()
                .filter(t -> !cachedMatchingIds.contains(t.getId()))
                .collect(Collectors.toList());
        // Add back all up-to-date matching records.
        finalMatching.addAll(cachedMatching);
    
        finalMatching.forEach(this::addToCache); // Cache all new data from the repository.
        return finalMatching;
    }
    
    @Override
    public void deleteById(ID id) {
        
        deleteFromCache(id);
        repository.deleteById(id);
    }
    
    @Override
    public void delete(T object) {
        
        deleteById(object.getId());
    }
    
    @Override
    @NotNull
    public Collection<T> getAllCached() {
        
        return cache.values();
    }
    
    @Override
    public void addToCache(T object) {
        
        cache.putIfAbsent(object.getId(), object);
    }
    
    @Override
    public void deleteFromCache(ID id) {
        
        cache.remove(id);
    }
}
