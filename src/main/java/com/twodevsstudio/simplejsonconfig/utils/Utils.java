package com.twodevsstudio.simplejsonconfig.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Utils {
    
    @NotNull
    public String colored(@NotNull String msg) {
        
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
