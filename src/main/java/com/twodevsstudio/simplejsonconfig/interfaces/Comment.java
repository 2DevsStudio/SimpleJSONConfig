package com.twodevsstudio.simplejsonconfig.interfaces;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Comment {

  String value();
}
