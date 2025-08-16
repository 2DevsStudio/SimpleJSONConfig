package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.lang.reflect.Type;

public class AdventureComponentAdapter implements JsonSerializer<Component>, JsonDeserializer<Component> {

  private static final GsonComponentSerializer SERIALIZER = GsonComponentSerializer.gson();

  @Override
  public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
    return JsonParser.parseString(SERIALIZER.serialize(src));
  }

  @Override
  public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return SERIALIZER.deserialize(json.toString());
  }
}