package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    private static final String CLASS_KEY = "SERIAL-ADAPTER-CLASS-KEY";

    private final Type seriType = new TypeToken<Map<String, Object>>() {
    }.getType();

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        Map<String, Object> itemData = context.deserialize(json, seriType);

        if (itemData.get("amount") != null) {
            Integer amount = ((Number) itemData.get("amount")).intValue();
            itemData.put("amount", amount);
        }

        ItemStack item = ItemStack.deserialize(itemData);

        if (itemData.containsKey("meta")) {
            Map<String, Object> itemmeta = (Map<String, Object>) itemData.get("meta");
            itemmeta = recursiveDoubleToInteger(itemmeta);


            ItemMeta meta = (ItemMeta) ConfigurationSerialization.deserializeObject(
                    itemmeta, Objects.requireNonNull(ConfigurationSerialization.getClassByAlias("ItemMeta")));

            Map<String, Object> attributes = (Map<String, Object>) itemmeta.get("attribute-modifiers");
            deserializeAttributes(attributes, meta);
            item.setItemMeta(meta);
        }

        return item;
    }


    @Override
    public JsonElement serialize(ItemStack item, Type typeOfSrc, JsonSerializationContext context) {

        Map<String, Object> jsonSerialized = getJsonSerialized(item);
        return context.serialize(jsonSerialized);
    }

    private Map<String, Object> getJsonSerialized(@NotNull ItemStack item) {

        Map<String, Object> serial = item.serialize();

        if (serial.get("meta") != null) {
            serial.put("meta", serializeMeta(Objects.requireNonNull(item.getItemMeta())));
        }
        return serial;
    }

    private void deserializeAttributes(Map<String, Object> attributes, ItemMeta meta) {

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {

            String attributeName = entry.getKey();

            Attribute attribute = Attribute.valueOf(attributeName);
            List<Map<String, Object>> rawModifiers = (List<Map<String, Object>>) entry.getValue();
            List<AttributeModifier> modifiers = new ArrayList<>();

            for (Map<String, Object> rawModifier : rawModifiers) {

                UUID modifierUUID = UUID.fromString(String.valueOf(rawModifier.get("uuid")));
                String modifierName = String.valueOf(rawModifier.get("name"));
                double modifierAmount = (double) rawModifier.get("amount");
                Operation modifierOperation = Operation.valueOf(String.valueOf(rawModifier.get("operation")));


                EquipmentSlot modifierSlot = null;
                Object rawSlot = rawModifier.get("slot");
                if (rawSlot != null) {
                    modifierSlot = EquipmentSlot.valueOf(String.valueOf(rawSlot));
                }

                modifiers.add(new AttributeModifier(modifierUUID, modifierName, modifierAmount, modifierOperation, modifierSlot));
            }
            modifiers.forEach(modifier -> meta.addAttributeModifier(attribute, modifier));
        }
    }

    private Map<String, Object> serializeMeta(@NotNull ItemMeta itemMeta) {

        Map<String, Object> originalMeta = itemMeta.serialize();
        Map<String, Object> meta = new HashMap<>(originalMeta);

        for (Map.Entry<String, Object> entry : meta.entrySet()) {

            String key = entry.getKey();
            Object object = entry.getValue();
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
            }
        }

        map.put(CLASS_KEY, ConfigurationSerialization.getAlias(configurationSerializable.getClass()));
        return map;
    }

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

}
