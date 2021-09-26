package com.twodevsstudio.simplejsonconfig.utils;

import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

  @NotNull
  public Component coloredComponent(@NotNull String msg) {
    return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
  }

  @NotNull
  public List<Component> coloredComponent(@NotNull List<String> msg) {
    return msg.stream().map(TextUtility::coloredComponent).collect(Collectors.toList());
  }

  public String textComponentAsString(Component component) {

    return LegacyComponentSerializer.legacyAmpersand().serialize(component);
  }

  public List<String> textComponentAsString(List<Component> components) {

    return components.stream().map(TextUtility::textComponentAsString).collect(Collectors.toList());
  }
}
