package com.twodevsstudio.simplejsonconfig;

import com.google.gson.GsonBuilder;
import com.twodevsstudio.simplejsonconfig.def.DefaultGsonBuilder;
import com.twodevsstudio.simplejsonconfig.def.Serializer;
import com.twodevsstudio.simplejsonconfig.utils.CustomLogger;
import com.twodevsstudio.simplejsonconfig.utils.Utils;
import org.bukkit.Material;
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
    getCommand("test").setExecutor(this);
  }
  
  @Override
  public boolean onCommand(@NotNull CommandSender sender,
                           @NotNull Command command,
                           @NotNull String label,
                           @NotNull String[] args
  ) {
  
    Player player = (Player) sender;
    ItemStack itemStack = new ItemStack(Material.DIRT);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.setDisplayName(Utils.colored("&6This is Dirt"));
    itemMeta.setLore(Utils.colored(Arrays.asList("&6This is Lore #1", "&6This is Lore #2")));
    
    player.getInventory().addItem(itemStack);
    
    File file = new File("test.json");
    GsonBuilder gsonBuilder = new DefaultGsonBuilder().getGsonBuilder();
    Serializer serializer = Serializer.getInst();
    serializer.setGson(gsonBuilder.create());
  
    serializer.saveConfig(itemStack, file);
  
    ItemStack load = serializer.loadConfig(ItemStack.class, file);
  
    player.getInventory().addItem(load);
  
    return true;
  }
}
