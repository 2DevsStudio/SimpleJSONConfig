package com.twodevsstudio.simplejsonconfig.interfaces;

import java.lang.annotation.*;

/**
 * @apiNote Annotate any class to make it a {@code Configuration}
 * Configuration classes have to extend a {@code Config} abstract class to inherit it's methods
 * and make it able to be processes by AnnotationProcessor
 */
@Target(ElementType.TYPE)
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface Configuration {
    
    /**
     * You have to specify your configuration name.
     *
     * @return name of the configuration
     *
     * @apiNote It's also the name of your configuration file
     */
    String value();
    
}
