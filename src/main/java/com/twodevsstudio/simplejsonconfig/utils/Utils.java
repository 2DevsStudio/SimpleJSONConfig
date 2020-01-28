package com.twodevsstudio.simplejsonconfig.utils;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class Utils {
    
    @NotNull
    public static String colored(@NotNull String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
    
    @NotNull
    public static String removeColor(@NotNull String string) {
        ChatColor[] colors = ChatColor.values();
        List<String> colorCodes = Arrays.stream(colors).map(chatColor -> "&" + chatColor.getChar()).collect(Collectors.toList());
        for (String color : colorCodes) {
            string = string.replaceAll(color, "");
            string = string.replaceAll(colored(color), "");
        }
        return string;
    }
    
}
