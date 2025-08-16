package com.twodevsstudio.simplejsonconfig.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for detecting Kyori Adventure availability on the classpath
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KyoriDetectionUtils {

    private static final String COMPONENT_CLASS = "net.kyori.adventure.text.Component";
    private static final String GSON_SERIALIZER_CLASS = "net.kyori.adventure.text.serializer.gson.GsonComponentSerializer";
    
    private static Boolean kyoriAvailable = null;

    /**
     * Checks if Kyori Adventure is available on the classpath
     * 
     * @return true if Kyori Adventure is available, false otherwise
     */
    public static boolean isKyoriAvailable() {
        if (kyoriAvailable == null) {
            kyoriAvailable = checkKyoriAvailability();
        }
        return kyoriAvailable;
    }

    private static boolean checkKyoriAvailability() {
        try {
            // Check for core Component class
            Class.forName(COMPONENT_CLASS);
            // Check for Gson serializer which we'll need for the adapter
            Class.forName(GSON_SERIALIZER_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}