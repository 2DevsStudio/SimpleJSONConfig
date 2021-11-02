package com.twodevsstudio.simplejsonconfig.exceptions;

public class PluginNotFoundException extends RuntimeException {

  public PluginNotFoundException() {
    super(
        "Cannot find your plugin instance. Please register plugin using 'SimpleJSONConfig.INSTANCE.register(this)' on top of your onEnable() method.");
  }

  public PluginNotFoundException(String message) {
    super(message);
  }

  public PluginNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public PluginNotFoundException(Throwable cause) {
    super(cause);
  }

  public PluginNotFoundException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
