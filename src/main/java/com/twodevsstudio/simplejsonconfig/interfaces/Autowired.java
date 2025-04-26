package com.twodevsstudio.simplejsonconfig.interfaces;

import java.lang.annotation.*;


/**
 * @apiNote Annotate any {@code static} field of configuration type inside your class to dynamically bind it to that
 * field
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Autowired {

}
