/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
