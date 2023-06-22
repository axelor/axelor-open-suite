package com.axelor.apps.base.service;

import com.axelor.apps.base.db.repo.FakerApiFieldRepository;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AnonymizerLineServiceImpl implements AnonymizerLineService {

  private FakerApiFieldRepository fakerApiFieldRepository;

  @Inject
  public AnonymizerLineServiceImpl(FakerApiFieldRepository fakerApiFieldRepository) {
    this.fakerApiFieldRepository = fakerApiFieldRepository;
  }

  /**
   * get faker api field domain including jsonField
   *
   * @param metaField
   * @param metaJsonField
   * @return
   */
  @Override
  public String getFakerApiFieldDomain(MetaField metaField, MetaJsonField metaJsonField) {
    String typeName;
    Map<String, String> jsonTypeMatchMap = getJsonTypeMatchMap();

    if (metaField.getJson()) {
      if (Objects.isNull(metaJsonField)) {
        return "1=1";
      }
      typeName = jsonTypeMatchMap.get(metaJsonField.getType());
    } else {
      typeName = metaField.getTypeName();
    }

    if (fakerApiFieldRepository
        .all()
        .filter("self.dataType = :dataType")
        .bind("dataType", typeName)
        .fetch()
        .isEmpty()) {
      return "1=1";
    }

    return "self.dataType = '" + typeName + "'";
  }

  protected Map<String, String> getJsonTypeMatchMap() {
    Map<String, String> map = new HashMap<>();
    map.put("string", "String");
    map.put("integer", "Integer");
    map.put("decimal", "BigDecimal");
    map.put("boolean", "Boolean");
    map.put("datetime", "LocalDateTime");
    map.put("date", "LocalDate");
    return map;
  }
}
