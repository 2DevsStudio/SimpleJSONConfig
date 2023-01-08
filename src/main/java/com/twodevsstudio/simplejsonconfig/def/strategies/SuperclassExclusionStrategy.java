package com.twodevsstudio.simplejsonconfig.def.strategies;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class SuperclassExclusionStrategy implements ExclusionStrategy {
    
    public boolean shouldSkipClass(Class<?> clazz) {
        
        return false;
    }
    
    public boolean shouldSkipField(@NotNull FieldAttributes fieldAttributes) {
        
        String fieldName = fieldAttributes.getName();
        Class<?> clazz = fieldAttributes.getDeclaringClass();
        
        return isFieldInSuperclass(clazz, fieldName);
    }
    
    private boolean isFieldInSuperclass(@NotNull Class<?> subclass, String fieldName) {
        
        Class<?> superclass = subclass.getSuperclass();
        
        while (superclass != null) {
            Field field = getField(superclass, fieldName);
            
            if (field != null) {
                return true;
            }
            
            superclass = superclass.getSuperclass();
        }
        
        return false;
    }
    
    @Nullable
    private Field getField(Class<?> clazz, String fieldName) {
        
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (Exception e) {
            return null;
        }
    }
    
}
