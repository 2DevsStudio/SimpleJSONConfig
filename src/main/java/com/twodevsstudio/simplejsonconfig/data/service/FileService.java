package com.twodevsstudio.simplejsonconfig.data.service;

import com.google.gson.reflect.TypeToken;
import com.twodevsstudio.simplejsonconfig.data.Identifiable;
import com.twodevsstudio.simplejsonconfig.data.cache.InMemoryCache;
import com.twodevsstudio.simplejsonconfig.data.repository.FileRepository;
import com.twodevsstudio.simplejsonconfig.def.StoreType;

import java.nio.file.Path;

public class FileService<ID, T extends Identifiable<ID>> extends StandardService<ID, T> {
    
    public FileService(Class<T> storedDataType, Path dataDirectory, StoreType storeType, InMemoryCache<ID, T> cache) {
        
        super(cache, new FileRepository<>(TypeToken.get(storedDataType), dataDirectory, storeType));
    }
}
