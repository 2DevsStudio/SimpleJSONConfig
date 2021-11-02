package com.twodevsstudio.simplejsonconfig.utils;

import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Utils {

  @NotNull
  public String colored(@NotNull String msg) {

    return ChatColor.translateAlternateColorCodes('&', msg);
  }

  @NotNull
  public List<String> colored(@NotNull List<String> msg) {

    return msg.stream().map(Utils::colored).collect(Collectors.toList());
  }

  @NotNull
  public String toAmpersand(@NotNull String msg) {

    return msg.replace("§", "&");
  }

  @NotNull
  public List<String> toAmpersand(@NotNull List<String> msg) {

    return msg.stream().map(Utils::toAmpersand).collect(Collectors.toList());
  }
}
