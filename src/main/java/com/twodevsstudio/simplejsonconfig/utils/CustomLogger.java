package com.twodevsstudio.simplejsonconfig.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class CustomLogger {

  public static String LOG_PREFIX = "[SimpleJSONConfig] »» ";

  public static String WARNING_PREFIX = "[WARNING] »» ";

  public static String ERROR_PREFIX = "[ERROR] »» ";

  public static void log(@NotNull String message) {
    Bukkit.getLogger().info(Utils.colored(LOG_PREFIX + message));
  }

  public static void warning(@NotNull String message) {
    Bukkit.getLogger().warning(Utils.colored(WARNING_PREFIX + message));
  }

  public static void warning(@NotNull Throwable throwable) {
    Bukkit.getLogger().warning(Utils.colored(WARNING_PREFIX + throwable.getMessage()));
    throwable.getCause().printStackTrace();
  }

  public static void error(@NotNull String message) {
    Bukkit.getLogger().severe(Utils.colored(ERROR_PREFIX + message));
  }

  public static void error(@NotNull Throwable throwable) {
    Bukkit.getLogger().severe(Utils.colored(ERROR_PREFIX + throwable.getMessage()));
    throwable.getCause().printStackTrace();
  }
}
