package com.axelor.apps.account.service.move.record.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class MoveContext {

  protected final Map<String, Object> valuesContext;
  protected final Map<String, Map<String, Object>> attrsContext;
  protected StringJoiner flashContext = new StringJoiner("\n");

  public MoveContext() {
    this.valuesContext = new HashMap<>();
    this.attrsContext = new HashMap<>();
  }

  public void putInValues(Map<String, Object> map) {
    Objects.requireNonNull(map);

    valuesContext.putAll(map);
  }

  public void putInValues(String key, Object value) {
    this.valuesContext.put(key, value);
  }

  public void putInAttrs(Map<String, Map<String, Object>> map) {
    Objects.requireNonNull(map);

    attrsContext.putAll(map);
  }

  public void putInAttrs(String fieldName, String attributeName, Object attributeValue) {
    this.attrsContext.putIfAbsent(fieldName, new HashMap<>());
    this.attrsContext.get(fieldName).put(attributeName, attributeValue);
  }

  public Map<String, Object> getValues() {
    return this.valuesContext;
  }

  public Map<String, Map<String, Object>> getAttrs() {
    return this.attrsContext;
  }

  public void putInFlash(String value) {
    this.flashContext.add(value);
  }

  public String getFlash() {
    return flashContext.toString();
  }

  public void merge(MoveContext moveContext) {
    if (moveContext != null) {
      this.attrsContext.putAll(moveContext.getAttrs());
      this.valuesContext.putAll(moveContext.getValues());
      this.flashContext.add(moveContext.getFlash());
    }
  }
}
