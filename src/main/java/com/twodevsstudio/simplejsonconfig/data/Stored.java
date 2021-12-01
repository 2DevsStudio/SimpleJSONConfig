package com.twodevsstudio.simplejsonconfig.data;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Stored {

  String value();
}
