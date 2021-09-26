package com.twodevsstudio.simplejsonconfig.utils;

import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class TextUtility {

  public String colorize(String toColor) {
    return ChatColor.translateAlternateColorCodes('&', toColor);
  }

  public List<String> colorize(List<String> toColor) {
    return toColor.stream().map(TextUtility::colorize).collect(Collectors.toList());
  }

  @NotNull
  public String toAmpersand(@NotNull String msg) {
    return msg.replace("§", "&");
  }

  @NotNull
  public List<String> toAmpersand(@NotNull List<String> msg) {
    return msg.stream().map(TextUtility::toAmpersand).collect(Collectors.toList());
  }
}
