package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.twodevsstudio.simplejsonconfig.utils.Utils;
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
import java.util.*;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
    
    private static final String CLASS_KEY = "SERIAL-ADAPTER-CLASS-KEY";
    
    private final Type seriType = new TypeToken<Map<String, Object>>() {
    }.getType();
    
    private final String AMOUNT_MEMBER = "amount";
    private final String META_MEMBER = "meta";
    private final String ATTRIBUTES_MEMBER = "attribute-modifiers";
    private final String DISPLAY_NAME_MEMBER = "display-name";
    private final String LORE_MEMBER = "lore";
    
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
            }
        }
        
        map.put(CLASS_KEY, ConfigurationSerialization.getAlias(configurationSerializable.getClass()));
        return map;
    }
    
    private void deserializeMeta(Map<String, Object> itemData, ItemStack item) {
        
        Map<String, Object> rawMeta = (Map<String, Object>) itemData.get(META_MEMBER);
        rawMeta = recursiveDoubleToInteger(rawMeta);
        
        String displayName = (String) rawMeta.get(DISPLAY_NAME_MEMBER);
        List<String> lore = (List<String>) rawMeta.get(LORE_MEMBER);
        
        //removing name and lore members to avoid incorrect meta deserialization
        rawMeta.remove(DISPLAY_NAME_MEMBER);
        rawMeta.remove(LORE_MEMBER);
        
        ItemMeta meta = (ItemMeta) ConfigurationSerialization.deserializeObject(
                rawMeta, Objects.requireNonNull(ConfigurationSerialization.getClassByAlias("ItemMeta")));
        
        meta.setDisplayName(Utils.colored(displayName));
        meta.setLore(Utils.colored(lore));
        
        Map<String, Object> attributes = (Map<String, Object>) rawMeta.getOrDefault(ATTRIBUTES_MEMBER, new HashMap<>());
        deserializeAttributes(attributes, meta);
        item.setItemMeta(meta);
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
            Attribute attribute = Attribute.valueOf(attributeName);
            
            List<Map<String, Object>> rawModifiers = (List<Map<String, Object>>) entry.getValue();
            List<AttributeModifier> modifiers = new ArrayList<>();
            
            rawModifiers.forEach(rawModifier -> modifiers.add(fromRawModifier(rawModifier)));
            modifiers.forEach(modifier -> meta.addAttributeModifier(attribute, modifier));
        }
    }
    
    @NotNull
    private AttributeModifier fromRawModifier(Map<String, Object> rawModifier) {
        
        UUID modifierUUID = UUID.fromString(String.valueOf(rawModifier.get("uuid")));
        String modifierName = String.valueOf(rawModifier.get("name"));
        double modifierAmount = (double) rawModifier.get(AMOUNT_MEMBER);
        Operation modifierOperation = Operation.valueOf(String.valueOf(rawModifier.get("operation")));
        
        EquipmentSlot modifierSlot = null;
        Object rawSlot = rawModifier.get("slot");
        if (rawSlot != null) {
            modifierSlot = EquipmentSlot.valueOf(String.valueOf(rawSlot));
        }
        
        return new AttributeModifier(modifierUUID, modifierName, modifierAmount, modifierOperation, modifierSlot);
    }
}
