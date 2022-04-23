package com.twodevsstudio.simplejsonconfig.data.cache;

import com.twodevsstudio.simplejsonconfig.GlobalCacheConfiguration;
import lombok.Data;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Data
public class InMemoryCache<K, V> {
    private final long entryLifespanMillis;
    private final long scanIntervalMillis;
    private final LRUMap cache;
    
    public InMemoryCache(GlobalCacheConfiguration cacheConfiguration) {
        
        this(cacheConfiguration.entryLifespanSeconds(), cacheConfiguration.scanIntervalSeconds(),
                cacheConfiguration.maxSize()
        );
    }
    
    public InMemoryCache(long entryLifespanSeconds, final long scanIntervalSeconds, int maxSize) {
        
        this.entryLifespanMillis = entryLifespanSeconds * 1000;
        this.scanIntervalMillis = scanIntervalSeconds * 1000;
        cache = new LRUMap(maxSize);
        
        if (entryLifespanMillis <= 0 || scanIntervalSeconds <= 0) {
            return;
        }
        
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(scanIntervalMillis);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                cleanupCache();
            }
        });
        
        thread.setDaemon(true);
        thread.start();
    }
    
    public void cleanupCache() {
        
        long now = System.currentTimeMillis();
        ArrayList<K> deleteKey;
        
        synchronized (cache) {
            MapIterator iterator = cache.mapIterator();
            
            deleteKey = new ArrayList<>((cache.size() / 2) + 1);
            K key;
            CacheObject cacheObject;
            
            while (iterator.hasNext()) {
                key = (K) iterator.next();
                cacheObject = (CacheObject) iterator.getValue();
                
                if (cacheObject != null && (now > (entryLifespanMillis + cacheObject.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }
        
        for (K key : deleteKey) {
            synchronized (cache) {
                cache.remove(key);
            }
            Thread.yield();
        }
    }
    
    public void put(K key, V value) {
        
        synchronized (cache) {
            cache.put(key, new CacheObject(value));
        }
    }
    
    public V get(K key) {
        
        synchronized (cache) {
            CacheObject cacheObject;
            cacheObject = (CacheObject) cache.get(key);
            
            if (cacheObject == null) {
                return null;
            }
            cacheObject.lastAccessed = System.currentTimeMillis();
            return cacheObject.value;
        }
    }
    
    public void remove(K key) {
        
        synchronized (cache) {
            cache.remove(key);
        }
    }
    
    public int size() {
        
        synchronized (cache) {
            return cache.size();
        }
    }
    
    public Collection<V> values() {
        
        synchronized (cache) {
            return ((Collection<CacheObject>) cache.values())
                    .stream()
                    .map(cacheObject -> cacheObject.value)
                    .collect(Collectors.toList());
        }
    }
    
    public boolean containsKey(K key) {
        
        synchronized (cache) {
            return cache.containsKey(key);
        }
    }
    
    @Data
    protected class CacheObject {
        
        private long lastAccessed = System.currentTimeMillis();
        private V value;
        
        public CacheObject(V value) {
            
            this.value = value;
        }
    }
}
