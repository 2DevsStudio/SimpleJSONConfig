package com.twodevsstudio.simplejsonconfig.def;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreType {
  JSON(".json"),
  YAML(".yml");

  private final String extension;
}
