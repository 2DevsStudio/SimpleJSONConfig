package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.common.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.twodevsstudio.simplejsonconfig.def.Serializer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ItemStackAdapter extends TypeAdapter<ItemStack> {
    
    private final static String CLASS_KEY = "SERIAL-ADAPTER-CLASS-KEY";
    private static Type seriType = new TypeToken<Map<String, Object>>() {
    
    }.getType();
    
    private Serializer serializer = Serializer.getInst();
    
    @Override
    public void write(JsonWriter jsonWriter, ItemStack itemStack) throws IOException {
        
        if (itemStack == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(getRaw(itemStack));
    }
    
    @Override
    public ItemStack read(@NotNull JsonReader jsonReader) throws IOException {
        
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        return fromRaw(jsonReader.nextString());
    }
    
    private String getRaw(@NotNull ItemStack item) {
        
        Map<String, Object> serial = item.serialize();
        
        if (serial.get("meta") != null) {
            ItemMeta itemMeta = item.getItemMeta();
            
            Map<String, Object> originalMeta = itemMeta.serialize();
            Map<String, Object> meta = new HashMap<>(originalMeta);
            
            for (Map.Entry<String, Object> entry : meta.entrySet()) {
                Object object = entry.getValue();
                if (object instanceof ConfigurationSerializable) {
                    ConfigurationSerializable serializable = (ConfigurationSerializable) object;
                    Map<String, Object> serialized = recursiveSerialization(serializable);
                    meta.put(entry.getKey(), serialized);
                }
            }
            serial.put("meta", meta);
        }
        
        return serializer.getGson().toJson(serial);
    }
    
    @Nullable
    private ItemStack fromRaw(String raw) {
        
        Map<String, Object> keys = Serializer.getInst().getGson().fromJson(raw, seriType);
        
        if (keys.get("amount") != null) {
            Integer amount = ((Number) keys.get("amount")).intValue();
            keys.put("amount", amount);
        }
        
        ItemStack item;
        try {
            item = ItemStack.deserialize(keys);
        } catch (Exception e) {
            return null;
        }
        
        if (keys.containsKey("meta")) {
            Map<String, Object> itemmeta = (Map<String, Object>) keys.get("meta");
            itemmeta = recursiveDoubleToInteger(itemmeta);
            ItemMeta meta = (ItemMeta) ConfigurationSerialization.deserializeObject(
                    itemmeta, Objects.requireNonNull(ConfigurationSerialization.getClassByAlias("ItemMeta")));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    @NotNull
    private static Map<String, Object> recursiveSerialization(@NotNull ConfigurationSerializable o) {
        
        Map<String, Object> originalMap = o.serialize();
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
        
        map.put(CLASS_KEY, ConfigurationSerialization.getAlias(o.getClass()));
        return map;
    }
    
    @NotNull
    private static Map<String, Object> recursiveDoubleToInteger(@NotNull Map<String, Object> originalMap) {
        
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
