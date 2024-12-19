package com.twodevsstudio.simplejsonconfig.exceptions;

import java.io.File;

public class ConfigNotFoundException extends RuntimeException {
    public ConfigNotFoundException(File configFile) {
        super("Config not found exception: " + configFile.getAbsolutePath());
    }
}
