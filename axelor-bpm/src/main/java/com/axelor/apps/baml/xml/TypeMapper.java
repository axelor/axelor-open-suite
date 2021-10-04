package com.axelor.apps.baml.xml;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class TypeMapper {

  public static Class<?> getType(String simpleType) {

    switch (simpleType) {
      case "integer":
        return Integer.class;
      case "boolean":
        return Boolean.class;
      case "string":
        return String.class;
      case "long":
        return Long.class;
      case "decimal":
        return BigDecimal.class;
      case "date":
        return LocalDate.class;
      case "datetime":
        return LocalDateTime.class;
      case "zdatetime":
        return ZonedDateTime.class;
      default:
        break;
    }

    return null;
  }
}
