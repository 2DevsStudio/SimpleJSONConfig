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
    
    private static Type seriType = new TypeToken<Map<String, Object>>() {
    
    }.getType();
    
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
            Map<String, Object> meta = new HashMap<>();
            for (Map.Entry<String, Object> entry : originalMeta.entrySet())
                meta.put(entry.getKey(), entry.getValue());
            Object o;
            for (Map.Entry<String, Object> entry : meta.entrySet()) {
                o = entry.getValue();
                if (o instanceof ConfigurationSerializable) {
                    ConfigurationSerializable serializable = (ConfigurationSerializable) o;
                    Map<String, Object> serialized = recursiveSerialization(serializable);
                    meta.put(entry.getKey(), serialized);
                }
            }
            serial.put("meta", meta);
        }
        
        return Serializer.getInst().getGson().toJson(serial);
    }
    
    @Nullable
    private ItemStack fromRaw(String raw) {
        Map<String, Object> keys = Serializer.getInst().getGson().fromJson(raw, seriType);
        
        if (keys.get("amount") != null) {
            Double d = (Double) keys.get("amount");
            Integer i = d.intValue();
            keys.put("amount", i);
        }
        
        ItemStack item;
        try {
            item = ItemStack.deserialize(keys);
        } catch (Exception e) {
            return null;
        }
        
        if (keys.containsKey("meta")) {
            @SuppressWarnings("unchecked") Map<String, Object> itemmeta = (Map<String, Object>) keys.get("meta");
            itemmeta = recursiveDoubleToInteger(itemmeta);
            ItemMeta meta = (ItemMeta) ConfigurationSerialization.deserializeObject(itemmeta, Objects.requireNonNull(ConfigurationSerialization.getClassByAlias("ItemMeta")));
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    private final static String CLASS_KEY = "SERIAL-ADAPTER-CLASS-KEY";
    
    @NotNull
    private static Map<String, Object> recursiveSerialization(@NotNull ConfigurationSerializable o) {
        Map<String, Object> originalMap = o.serialize();
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
            Object o2 = entry.getValue();
            if (o2 instanceof ConfigurationSerializable) {
                ConfigurationSerializable serializable = (ConfigurationSerializable) o2;
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
            Object o = entry.getValue();
            if (o instanceof Double) {
                Double d = (Double) o;
                Integer i = d.intValue();
                map.put(entry.getKey(), i);
            } else if (o instanceof Map) {
                @SuppressWarnings("unchecked") Map<String, Object> subMap = (Map<String, Object>) o;
                map.put(entry.getKey(), recursiveDoubleToInteger(subMap));
            } else {
                map.put(entry.getKey(), o);
            }
        }
        return map;
    }
    
}
