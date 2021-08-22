package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.temporal.ChronoUnit;

public class ChronoUnitAdapter implements JsonDeserializer<ChronoUnit>, JsonSerializer<ChronoUnit> {

  @Override
  public ChronoUnit deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return ChronoUnit.valueOf(json.getAsString());
  }

  @Override
  public JsonElement serialize(ChronoUnit src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src.name());
  }
}
