package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.*;

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
