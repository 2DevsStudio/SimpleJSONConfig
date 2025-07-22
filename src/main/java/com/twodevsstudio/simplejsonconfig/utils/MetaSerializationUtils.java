package com.twodevsstudio.simplejsonconfig.utils;

import com.google.gson.internal.LinkedTreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@UtilityClass
public class MetaSerializationUtils {

    public List<PotionEffect> deserializePotionEffects(List<Map<String, Object>> rawEffects) {

        return rawEffects.stream().map(MetaSerializationUtils::deserializeRawPotionEffect).collect(Collectors.toList());
    }

    public PotionEffect deserializeRawPotionEffect(Map<String, Object> rawEffect) {

        Map<String, Object> rawType = (Map<String, Object>) rawEffect.getOrDefault(
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


    public static List<FireworkEffect> deserializeFireworkEffects(List<Map<String, Object>> rawEffects) {

        return rawEffects.stream()
                .map(MetaSerializationUtils::deserializeRawFireworkEffect)
                .collect(Collectors.toList());
    }

    public FireworkEffect deserializeRawFireworkEffect(Map<String, Object> rawEffect) {

        ArrayList<Map<String, Object>> colors = (ArrayList<Map<String, Object>>) rawEffect.get(
                "colors");
        ArrayList<Map<String, Object>> fades = (ArrayList<Map<String, Object>>) rawEffect.get(
                "fadeColors");

        rawEffect.put("colors", deserializeRawColors(colors));
        rawEffect.put("fadeColors", deserializeRawColors(fades));

        return (FireworkEffect) FireworkEffect.deserialize(rawEffect);
    }

    public List<Color> deserializeRawColors(List<Map<String, Object>> rawColors) {

        return rawColors.stream().map(MetaSerializationUtils::deserializeRawColor).collect(Collectors.toList());
    }

    public Color deserializeRawColor(Map<String, Object> rawColor) {

        Number redNum;
        Number greenNum;
        Number blueNum;
        if (rawColor.containsKey("RED")) {
            redNum = ((Number) rawColor.remove("RED"));
            greenNum = ((Number) rawColor.remove("GREEN"));
            blueNum = ((Number) rawColor.remove("BLUE"));
        } else {
            redNum = ((Number) rawColor.remove("red"));
            greenNum = ((Number) rawColor.remove("green"));
            blueNum = ((Number) rawColor.remove("blue"));
        }

        final int red = redNum.intValue();
        final int green = greenNum.intValue();
        final int blue = blueNum.intValue();
        rawColor.put("RED", Math.abs(red));
        rawColor.put("GREEN", Math.abs(green));
        rawColor.put("BLUE", Math.abs((blue)));

        return Color.deserialize(rawColor);
    }
}

