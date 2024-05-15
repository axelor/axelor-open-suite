package com.axelor.apps.sale.helper;

import java.util.HashMap;
import java.util.Map;

public final class SaleOrderLineHelper {
  public static void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }
}
