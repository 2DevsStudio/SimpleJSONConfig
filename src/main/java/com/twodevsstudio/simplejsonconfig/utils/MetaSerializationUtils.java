package com.twodevsstudio.simplejsonconfig.utils;

import com.google.gson.internal.LinkedTreeMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@UtilityClass
public class MetaSerializationUtils {

  public List<PotionEffect> deserializePotionEffects(
      List<LinkedTreeMap<String, Object>> rawEffects) {

    return rawEffects.stream().map(MetaSerializationUtils::deserializeRawPotionEffect)
        .collect(Collectors.toList());
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

}
