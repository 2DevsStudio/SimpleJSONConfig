package com.twodevsstudio.simplejsonconfig.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

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
    public Component coloredComponent(@NotNull String msg) {
        
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }
    
    @NotNull
    public List<Component> coloredComponent(@NotNull List<String> msg) {
        
        return msg.stream().map(Utils::coloredComponent).collect(Collectors.toList());
    }
    
    public String textComponentAsString(Component component) {
        
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }
    
    public List<String> textComponentAsString(List<Component> components) {
        
        return components.stream().map(Utils::textComponentAsString).collect(Collectors.toList());
    }
}
