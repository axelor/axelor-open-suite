package com.axelor.apps.base.utils;

import java.util.HashMap;
import java.util.Map;

public class MapTools {

  private MapTools() {}

  public static void addMap(
      Map<String, Map<String, Object>> map, Map<String, Map<String, Object>> toAddMap) {
    for (Map.Entry<String, Map<String, Object>> entry : toAddMap.entrySet()) {
      if (map.containsKey(entry.getKey())) {
        Map<String, Object> newMap = new HashMap<>();
        newMap.putAll(toAddMap.get(entry.getKey()));
        newMap.putAll(map.get(entry.getKey()));
        map.put(entry.getKey(), newMap);
      } else {
        map.put(entry.getKey(), entry.getValue());
      }
    }
  }
}
