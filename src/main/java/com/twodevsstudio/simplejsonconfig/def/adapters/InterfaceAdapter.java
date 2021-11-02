package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.twodevsstudio.simplejsonconfig.interfaces.PostProcessable;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;

public class InterfaceAdapter implements JsonSerializer, JsonDeserializer {

  private static final String CLASSNAME = "CLASSNAME";

  private static final String DATA = "DATA";

  public Object deserialize(
      @NotNull JsonElement jsonElement,
      Type type,
      @NotNull JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {

    JsonObject jsonObject = jsonElement.getAsJsonObject();
    JsonPrimitive primitive = (JsonPrimitive) jsonObject.get(CLASSNAME);
    String className = primitive.getAsString();
    Class clazz = getObjectClass(className);

    Object deserializedObject = jsonDeserializationContext.deserialize(jsonObject.get(DATA), clazz);
    if (deserializedObject instanceof PostProcessable) {
      ((PostProcessable) deserializedObject).gsonPostProcess();
    }

    return deserializedObject;
  }

  public JsonElement serialize(
      @NotNull Object jsonElement,
      Type type,
      @NotNull JsonSerializationContext jsonSerializationContext) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(CLASSNAME, jsonElement.getClass().getName());
    jsonObject.add(DATA, jsonSerializationContext.serialize(jsonElement));

    return jsonObject;
  }

  private Class getObjectClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new JsonParseException(e.getMessage());
    }
  }
}
