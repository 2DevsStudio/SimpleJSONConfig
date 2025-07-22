package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.*;
import lombok.SneakyThrows;
import org.bukkit.World;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;

public class ReferenceAdapter implements JsonSerializer, JsonDeserializer {

  @Override
  public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {

    JsonObject jsonObj = new JsonObject();
    Reference reference = (Reference) src;
    Object ref = reference.get();
    if (ref instanceof World) {
      World world = (World) ref;
      return context.serialize(world, world.getClass());
    } else {
      jsonObj.add("reference", context.serialize(ref.getClass().getName()));
      jsonObj.add("value", context.serialize(ref, ref.getClass()));
    }
    return jsonObj;
  }

  @SneakyThrows
  @Override
  public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {

    JsonObject jsonObject = json.getAsJsonObject();
    Object value;

    if (!jsonObject.has("reference")) {
      value = context.deserialize(jsonObject, World.class);
    } else {
      JsonElement reference = jsonObject.get("reference");
      Class<?> aClass = Class.forName(reference.getAsString());
      value = context.deserialize(jsonObject.get("value"), aClass);
    }

    return new WeakReference<>(value);
  }
}
