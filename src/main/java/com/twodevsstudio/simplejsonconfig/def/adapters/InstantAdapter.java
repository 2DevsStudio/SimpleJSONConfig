package com.twodevsstudio.simplejsonconfig.def.adapters;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Instant;

public class InstantAdapter extends TypeAdapter<Instant> {

  @Override
  public void write(JsonWriter out, Instant value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    out.value(value.toString()); // ISO-8601
  }

  @Override
  public Instant read(JsonReader in) throws IOException {
    JsonToken token = in.peek();
    if (token == JsonToken.NULL) {
      in.nextNull();
      return null;
    }
    if (token == JsonToken.STRING) {
      return Instant.parse(in.nextString());
    }
    if (token == JsonToken.NUMBER) {
      long v = in.nextLong();
      if (v > 3_000_000_000L) {
        return Instant.ofEpochMilli(v);
      }
      return Instant.ofEpochSecond(v);
    }
    throw new JsonParseException("Expected Instant as ISO-8601 string or epoch number, got: " + token);
  }
}

