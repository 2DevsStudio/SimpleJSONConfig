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
        
        List<T> matching = repository.findAll().stream().filter(predicate).collect(Collectors.toList());
        matching.forEach(this::addToCache);
        return matching;
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
