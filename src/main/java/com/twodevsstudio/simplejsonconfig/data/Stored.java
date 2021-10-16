package com.twodevsstudio.simplejsonconfig.data;

import com.twodevsstudio.simplejsonconfig.def.StoreType;

import java.lang.annotation.*;

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface Stored {
    
    String value();
    
    StoreType storeType() default StoreType.JSON;
}
