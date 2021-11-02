package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class WorldAdapter implements JsonSerializer, JsonDeserializer {

  public Object deserialize(
      @NotNull JsonElement jsonElement,
      Type type,
      JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return Bukkit.getWorld(jsonElement.getAsJsonObject().get("name").getAsString());
  }

  public JsonElement serialize(
      Object jsonElement, Type type, @NotNull JsonSerializationContext jsonSerializationContext) {
    JsonObject jsonObject = new JsonObject();
    World world = (World) jsonElement;
    jsonObject.add("name", jsonSerializationContext.serialize(world.getName()));
    return jsonObject;
  }
}
