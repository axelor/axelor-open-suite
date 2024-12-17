package com.axelor.apps.base.utils;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;

public class ConfiguratorGeneratorMapToObject {

  /**
   * check field and fill property
   *
   * @param model
   * @param mapper
   * @param property
   * @param value
   */
  public static void fillObjectPropertyIfExists(
      AuditableModel model, Mapper mapper, String property, Object value) {
    Property objectProperty = mapper.getProperty(property);

    if (objectProperty == null || !objectProperty.getJavaType().isInstance(value)) {
      return;
    }

    mapper.set(model, property, value);
  }
}
