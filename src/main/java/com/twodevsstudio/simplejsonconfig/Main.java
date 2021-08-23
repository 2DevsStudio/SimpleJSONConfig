package com.twodevsstudio.simplejsonconfig;

import com.google.gson.GsonBuilder;
import com.twodevsstudio.simplejsonconfig.def.DefaultGsonBuilder;
import com.twodevsstudio.simplejsonconfig.def.Serializer;
import com.twodevsstudio.simplejsonconfig.utils.CustomLogger;
import com.twodevsstudio.simplejsonconfig.utils.Utils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;

public class Main extends JavaPlugin {
    
    @Override
    public void onEnable() {
        
        CustomLogger.log("Enabling SimpleJsonConfig - 2DevsStudio");
    }
}
