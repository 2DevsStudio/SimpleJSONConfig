package com.twodevsstudio.simplejsonconfig.def.strategies;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class SuperclassExclusionStrategy implements ExclusionStrategy {
    
    public boolean shouldSkipClass(Class<?> arg0) {
        return false;
    }
    
    public boolean shouldSkipField(@NotNull FieldAttributes fieldAttributes) {
        String fieldName = fieldAttributes.getName();
        Class<?> theClass = fieldAttributes.getDeclaringClass();
        
        return isFieldInSuperclass(theClass, fieldName);
    }
    
    private boolean isFieldInSuperclass(@NotNull Class<?> subclass, String fieldName) {
        Class<?> superclass = subclass.getSuperclass();
        Field field;
        
        while (superclass != null) {
            field = getField(superclass, fieldName);
            
            if (field != null)
                return true;
            
            superclass = superclass.getSuperclass();
        }
        
        return false;
    }
    
    @Nullable
    private Field getField(Class<?> theClass, String fieldName) {
        try {
            return theClass.getDeclaredField(fieldName);
        } catch (Exception e) {
            return null;
        }
    }
    
}
