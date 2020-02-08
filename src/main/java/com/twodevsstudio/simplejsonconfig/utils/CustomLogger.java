package com.twodevsstudio.simplejsonconfig.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class CustomLogger {
    
    public static String LOG_PREFIX = "&7[&aSimpleJSONConfig&7] &a»» &7";
    
    public static String WARNING_PREFIX = "&7[&eWARNING&7] &a»» &e";
    
    public static String ERROR_PREFIX = "&7[&4ERROR&7] &c»» &4";
    
    public static void log(@NotNull String message) {
        Bukkit.getConsoleSender().sendMessage(Utils.colored(LOG_PREFIX + message));
    }
    
    public static void warning(@NotNull String message) {
        Bukkit.getConsoleSender().sendMessage(Utils.colored(WARNING_PREFIX + message));
    }
    
    public static void warning(@NotNull Throwable throwable) {
        Bukkit.getConsoleSender().sendMessage(Utils.colored(WARNING_PREFIX + throwable.getMessage()));
        throwable.getCause().printStackTrace();
    }
    
    public static void error(@NotNull String message) {
        Bukkit.getConsoleSender().sendMessage(Utils.colored(ERROR_PREFIX + message));
    }
    
    
    public static void error(@NotNull Throwable throwable) {
        Bukkit.getConsoleSender().sendMessage(Utils.colored(ERROR_PREFIX + throwable.getMessage()));
        throwable.getCause().printStackTrace();
    }
    
}
