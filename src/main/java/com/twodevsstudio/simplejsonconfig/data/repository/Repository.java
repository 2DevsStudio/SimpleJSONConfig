package com.twodevsstudio.simplejsonconfig.data.repository;

import com.twodevsstudio.simplejsonconfig.data.Identifiable;

import java.util.Collection;

public interface Repository<ID, T extends Identifiable<ID>> {
    
    void save(T object);
    
    T findById(ID id);
    
    Collection<T> findAll();
    
    void deleteById(ID id);
    
    void delete(T object);
}
