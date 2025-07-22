package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.twodevsstudio.simplejsonconfig.utils.MetaSerializationUtils;
import com.twodevsstudio.simplejsonconfig.utils.Utils;
import java.lang.reflect.Type;
import java.util.*;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    private static final String CLASS_KEY = "SERIAL-ADAPTER-CLASS-KEY";

    private final Type seriType = new TypeToken<Map<String, Object>>() {
    }.getType();

    private final String META_TYPE_MEMBER = "meta-type";
    private final String AMOUNT_MEMBER = "amount";
    private final String META_MEMBER = "meta";
    private final String ATTRIBUTES_MEMBER = "attribute-modifiers";
    private final String DISPLAY_NAME_MEMBER = "display-name";
    private final String LORE_MEMBER = "lore";
    private final String CUSTOM_EFFECTS_MEMBER = "custom-effects";
    private final String CUSTOM_COLOR_MEMBER = "custom-color";
    private final String FIREWORK_EFFECTS_MEMBER = "firework-effects";
    private final String FIREWORK_EFFECT_MEMBER = "firework-effect";
    private final String COLOR_MEMBER = "color";
    private final String DISPLAY_MAP_COLOR_MEMBER = "display-map-color";

    @Override
    public JsonElement serialize(ItemStack item, Type typeOfSrc, JsonSerializationContext context) {

        Map<String, Object> jsonSerialized = getJsonSerialized(item);
        return context.serialize(jsonSerialized);
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        Map<String, Object> itemData = context.deserialize(json, seriType);

        if (itemData.get(AMOUNT_MEMBER) != null) {
            Integer amount = ((Number) itemData.get(AMOUNT_MEMBER)).intValue();
            itemData.put(AMOUNT_MEMBER, amount);
        }

        ItemStack item = ItemStack.deserialize(itemData);

        if (itemData.containsKey(META_MEMBER)) {
            deserializeMeta(itemData, item);
        }

        return item;
    }

    private Map<String, Object> getJsonSerialized(@NotNull ItemStack item) {

        Map<String, Object> serial = item.serialize();

        if (serial.get(META_MEMBER) != null) {
            serial.put(META_MEMBER, serializeMeta(Objects.requireNonNull(item.getItemMeta())));
        }
        return serial;
    }

    private Map<String, Object> serializeMeta(@NotNull ItemMeta itemMeta) {

        Map<String, Object> originalMeta = itemMeta.serialize();
        Map<String, Object> meta = new HashMap<>(originalMeta);

        for (Map.Entry<String, Object> entry : meta.entrySet()) {

            String key = entry.getKey();
            Object object = entry.getValue();

            //Plain strings with ampersand color codes instead of Text Components
            if (key.equalsIgnoreCase(DISPLAY_NAME_MEMBER)) {
                meta.put(key, Utils.toAmpersand(itemMeta.getDisplayName()));
                continue;
            }

            if (key.equalsIgnoreCase(LORE_MEMBER)) {
                meta.put(key, Utils.toAmpersand(itemMeta.getLore()));
                continue;
            }

            if (object instanceof ConfigurationSerializable) {
                ConfigurationSerializable serializable = (ConfigurationSerializable) object;
                Map<String, Object> serialized = recursiveSerialization(serializable);
                meta.put(key, serialized);
            }
        }
        return meta;
    }

    @NotNull
    private Map<String, Object> recursiveSerialization(@NotNull ConfigurationSerializable configurationSerializable) {

        Map<String, Object> originalMap = configurationSerializable.serialize();
        Map<String, Object> map = new HashMap<>();

        for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
            Object object = entry.getValue();
            if (object instanceof ConfigurationSerializable) {
                ConfigurationSerializable serializable = (ConfigurationSerializable) object;
                Map<String, Object> newMap = recursiveSerialization(serializable);
                newMap.put(CLASS_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
                map.put(entry.getKey(), newMap);
            } else {
                map.put(entry.getKey(), object);
            }
        }

        map.put(CLASS_KEY, ConfigurationSerialization.getAlias(configurationSerializable.getClass()));
        return map;
    }

    private void deserializeMeta(Map<String, Object> itemData, ItemStack item) {

        Map<String, Object> rawMeta = (Map<String, Object>) itemData.get(META_MEMBER);
        String metaType = (String) rawMeta.get(META_TYPE_MEMBER);

        rawMeta = recursiveDoubleToInteger(rawMeta);

        String displayName = (String) rawMeta.get(DISPLAY_NAME_MEMBER);
        List<String> lore = (List<String>) rawMeta.get(LORE_MEMBER);

        //removing name and lore members to avoid incorrect meta deserialization
        rawMeta.remove(DISPLAY_NAME_MEMBER);
        rawMeta.remove(LORE_MEMBER);

        deserializeMetaTypeData(rawMeta, metaType);

        ItemMeta meta = (ItemMeta) ConfigurationSerialization.deserializeObject(
                rawMeta, Objects.requireNonNull(ConfigurationSerialization.getClassByAlias("ItemMeta")));

        if (displayName != null) {
            meta.setDisplayName(Utils.colored(displayName));
        }
        if (lore != null) {
            meta.setLore(Utils.colored(lore));
        }

        Map<String, Object> attributes = (Map<String, Object>) rawMeta.getOrDefault(ATTRIBUTES_MEMBER, new HashMap<>());
        deserializeAttributes(attributes, meta);
        item.setItemMeta(meta);
    }

    private void deserializeMetaTypeData(Map<String, Object> rawMeta, String metaType) {

        if (metaType.equalsIgnoreCase("POTION")) {

            List<PotionEffect> potionEffects = MetaSerializationUtils.deserializePotionEffects(
                    (List<Map<String, Object>>) rawMeta.get(CUSTOM_EFFECTS_MEMBER));
            Color customColor = MetaSerializationUtils.deserializeRawColor(
                    (Map<String, Object>) rawMeta.get(CUSTOM_COLOR_MEMBER));

            rawMeta.put(CUSTOM_EFFECTS_MEMBER, potionEffects);
            rawMeta.put(CUSTOM_COLOR_MEMBER, customColor);

        } else if (metaType.equalsIgnoreCase("FIREWORK")) {

            List<FireworkEffect> fireworkEffects = MetaSerializationUtils.deserializeFireworkEffects(
                    (List<Map<String, Object>>) rawMeta.get(FIREWORK_EFFECTS_MEMBER));

            rawMeta.put(FIREWORK_EFFECTS_MEMBER, fireworkEffects);
        } else if (metaType.equalsIgnoreCase("FIREWORK_EFFECT")) {

            FireworkEffect fireworkEffect = MetaSerializationUtils.deserializeRawFireworkEffect(
                    (Map<String, Object>) rawMeta.get(FIREWORK_EFFECT_MEMBER));

            rawMeta.put(FIREWORK_EFFECT_MEMBER, fireworkEffect);
        } else if (metaType.equalsIgnoreCase("LEATHER_ARMOR")) {

            Color color = MetaSerializationUtils.deserializeRawColor(
                    (Map<String, Object>) rawMeta.get(COLOR_MEMBER));

            rawMeta.put(COLOR_MEMBER, color);
        } else if (metaType.equalsIgnoreCase("MAP")) {

            Color displayMapColor = MetaSerializationUtils.deserializeRawColor(
                    (Map<String, Object>) rawMeta.get(DISPLAY_MAP_COLOR_MEMBER));

            rawMeta.put(DISPLAY_MAP_COLOR_MEMBER, displayMapColor);
        }
    }

    /**
     * ItemStack deserialization does not support double values in most cases This method recursively converts all
     * double values to integer in original and nested maps
     *
     * @return Original map with converted doubles to integers
     */
    @NotNull
    private Map<String, Object> recursiveDoubleToInteger(@NotNull Map<String, Object> originalMap) {

        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : originalMap.entrySet()) {

            Object object = entry.getValue();

            if (object instanceof Double) {
                int number = ((Double) object).intValue();
                map.put(entry.getKey(), number);

            } else if (object instanceof Map) {
                Map<String, Object> subMap = (Map<String, Object>) object;
                map.put(entry.getKey(), recursiveDoubleToInteger(subMap));
            } else {
                map.put(entry.getKey(), object);
            }
        }
        return map;
    }

    private void deserializeAttributes(Map<String, Object> attributes, ItemMeta meta) {
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {

            String attributeName = entry.getKey();
            Attribute attribute = Registry.ATTRIBUTE.getOrThrow(NamespacedKey.minecraft(attributeName));

            List<Map<String, Object>> rawModifiers = (List<Map<String, Object>>) entry.getValue();
            List<AttributeModifier> modifiers = new ArrayList<>();

            for (Map<String, Object> rawModifier : rawModifiers) {
                deserializeModifier(rawModifier, modifiers);
            }
            modifiers.forEach(modifier -> meta.addAttributeModifier(attribute, modifier));
        }
    }

    private void deserializeModifier(Map<String, Object> rawModifier,
                                     List<AttributeModifier> modifiers
    ) {

        NamespacedKey rawKey = NamespacedKey.fromString((String) rawModifier.get("key"));
        double modifierAmount = (double) rawModifier.get(AMOUNT_MEMBER);
        Operation modifierOperation = Operation.valueOf(String.valueOf(rawModifier.get("operation")));
        EquipmentSlotGroup modifierSlot = EquipmentSlotGroup.getByName((String) rawModifier.get("slot"));

        if (modifierSlot == null) {
            modifierSlot = EquipmentSlotGroup.HAND;
        }
        modifiers.add(
                new AttributeModifier(rawKey, modifierAmount, modifierOperation, modifierSlot));
    }
}
