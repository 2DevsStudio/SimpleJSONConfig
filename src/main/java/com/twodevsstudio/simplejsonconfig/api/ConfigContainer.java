package com.twodevsstudio.simplejsonconfig.api;

import java.util.HashMap;
import java.util.Map;

class ConfigContainer {

  static final Map<Class<? extends Config>, Config> SINGLETONS = new HashMap<>();

  static <T extends Config> T getConfiguration(Class<T> configClass) {

    if (!SINGLETONS.containsKey(configClass)) {
      return null;
    }

    return (T) SINGLETONS.get(configClass);
  }
}
