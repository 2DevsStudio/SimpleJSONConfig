package com.twodevsstudio.simplejsonconfig.utils;

import com.google.gson.internal.LinkedTreeMap;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class MetaSerializationUtils {
    
    public List<PotionEffect> deserializePotionEffects(List<LinkedTreeMap<String, Object>> rawEffects) {
        
        return rawEffects.stream().map(MetaSerializationUtils::deserializeRawPotionEffect).collect(Collectors.toList());
    }
    
    public PotionEffect deserializeRawPotionEffect(LinkedTreeMap<String, Object> rawEffect) {
        
        LinkedTreeMap<String, Object> rawType = (LinkedTreeMap<String, Object>) rawEffect.getOrDefault(
                "type", new LinkedTreeMap<>());
        int typeId = ((Double) rawType.getOrDefault("id", 1D)).intValue();
        
        PotionEffectType type = PotionEffectType.getById(typeId);
        if (type == null) {
            type = PotionEffectType.GLOWING;
        }
        
        int duration = ((Double) rawEffect.getOrDefault("duration", 1D)).intValue();
        int amplifier = ((Double) rawEffect.getOrDefault("amplifier", 1D)).intValue();
        boolean ambient = (boolean) rawEffect.getOrDefault("ambient", true);
        boolean particles = (boolean) rawEffect.getOrDefault("particles", true);
        boolean icon = (boolean) rawEffect.getOrDefault("icon", true);
        
        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
    }
    
    
    public static List<FireworkEffect> deserializeFireworkEffects(List<LinkedTreeMap<String, Object>> rawEffects) {
        
        return rawEffects.stream()
                .map(MetaSerializationUtils::deserializeRawFireworkEffect)
                .collect(Collectors.toList());
    }
    
    public FireworkEffect deserializeRawFireworkEffect(LinkedTreeMap<String, Object> rawEffect) {
        
        ArrayList<LinkedTreeMap<String, Object>> colors = (ArrayList<LinkedTreeMap<String, Object>>) rawEffect.get(
                "colors");
        ArrayList<LinkedTreeMap<String, Object>> fades = (ArrayList<LinkedTreeMap<String, Object>>) rawEffect.get(
                "fadeColors");
    
        rawEffect.put("colors", deserializeRawColors(colors));
        rawEffect.put("fadeColors", deserializeRawColors(fades));
        
        return (FireworkEffect) FireworkEffect.deserialize(rawEffect);
    }
    
    public List<Color> deserializeRawColors(List<LinkedTreeMap<String, Object>> rawColors) {
        
        return rawColors.stream().map(MetaSerializationUtils::deserializeRawColor).collect(Collectors.toList());
    }
    
    public Color deserializeRawColor(LinkedTreeMap<String, Object> rawColor) {
        final int red = ((Double) rawColor.remove("red")).intValue();
        final int green = ((Double) rawColor.remove("green")).intValue();
        final int blue = ((Double) rawColor.remove("blue")).intValue();
        rawColor.put("RED", Math.abs(red));
        rawColor.put("GREEN", Math.abs(green));
        rawColor.put("BLUE", Math.abs((blue)));
        
        return Color.deserialize(rawColor);
    }
}

