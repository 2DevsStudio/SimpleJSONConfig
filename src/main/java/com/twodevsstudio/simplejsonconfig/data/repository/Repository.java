package com.twodevsstudio.simplejsonconfig.data.repository;

import com.twodevsstudio.simplejsonconfig.data.Identifiable;

import java.nio.charset.Charset;
import java.util.Collection;

public interface Repository<ID, T extends Identifiable<ID>> {
    
    void save(T object);
    
    void save(T object, Charset charset);
    
    T findById(ID id);
    
    Collection<T> findAll();
    
    void deleteById(ID id);
    
    void delete(T object);
}
