package com.twodevsstudio.simplejsonconfig.interfaces;

import java.lang.annotation.*;

/**
 * @apiNote Annotate any class to make it a {@code Configuration} Configuration classes have to
 *     extend a {@code Config} abstract class to inherit its methods and make it able to be
 *     processes by AnnotationProcessor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {

  /**
   * You have to specify your configuration name.
   *
   * @return name of the configuration
   * @apiNote It's also the name of your configuration file
   */
  String value();

  /**
   * You can specify a path to your configuration file
   *
   * @implNote Child path, starting from the plugins folder, example: "/simplejsonconfig", if not
   *     specified it will be the default to directory set in SimpleJSONConfig initializing
   *     (SimpleJSONConfig.INSTANCE.register)
   */
  String configPath() default "";

  /**
   * If true: Enables automatic config updates for existing configuration files After changing the
   * definition of the config class, changes will be automatically applied to existing configuration
   * files. ie. After adding new field to the configuration it will be automatically written to your
   * config file with default value (or null if default was not specified)
   */
  boolean enableConfigAutoUpdates() default true;
}
