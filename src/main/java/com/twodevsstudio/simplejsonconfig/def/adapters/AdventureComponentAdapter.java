package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.lang.reflect.Type;

public class AdventureComponentAdapter
    implements JsonSerializer<Component>, JsonDeserializer<Component> {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  @Override
  public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(MINI_MESSAGE.serialize(src));
  }

  @Override
  public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    if (json == null || json.isJsonNull()) {
      return Component.empty();
    }
    if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
      throw new JsonParseException("Expected MiniMessage string for Component");
    }
    return MINI_MESSAGE.deserialize(json.getAsString());
  }
}
