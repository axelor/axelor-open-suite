/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.MetaJsonField;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfiguratorCreatorImportUtilsServiceImpl
    implements ConfiguratorCreatorImportUtilsService {

  @Override
  public void fixAttributesName(ConfiguratorCreator creator) throws AxelorException {
    List<MetaJsonField> attributes = creator.getAttributes();
    if (attributes == null) {
      return;
    }
    for (MetaJsonField attribute : attributes) {
      String name = attribute.getName();
      if (name != null) {
        name = name.replace("$AXELORTMP", "");
        if (name.contains("_")) {
          attribute.setName(name.substring(0, name.lastIndexOf('_')) + '_' + creator.getId());
        }
      }
      updateOtherFieldsInAttribute(creator, attribute);
      updateAttributeNameInFormulas(creator, name, attribute.getName());
    }
  }

  /**
   * Update the configurator id in other fields of the attribute.
   *
   * @param creator
   * @param attribute attribute to update
   */
  protected void updateOtherFieldsInAttribute(
      ConfiguratorCreator creator, MetaJsonField attribute) {
    try {
      List<Field> fieldsToUpdate =
          Arrays.stream(attribute.getClass().getDeclaredFields())
              .filter(field -> field.getType().equals(String.class))
              .collect(Collectors.toList());
      for (Field field : fieldsToUpdate) {
        Mapper mapper = Mapper.of(attribute.getClass());
        Method getter = mapper.getGetter(field.getName());
        String fieldString = (String) getter.invoke(attribute);

        if (fieldString != null && fieldString.contains("_")) {
          String updatedFieldString = updateFieldIds(fieldString, creator.getId());
          Method setter = mapper.getSetter(field.getName());
          setter.invoke(attribute, updatedFieldString);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  protected String updateFieldIds(String fieldString, Long id) {

    Pattern attributePattern = Pattern.compile("\\w+_\\d+");
    Matcher matcher = attributePattern.matcher(fieldString);
    StringBuffer result = new StringBuffer();

    while (matcher.find()) {
      matcher.appendReplacement(result, matcher.group().replaceAll("_\\d+", "_" + id));
    }

    matcher.appendTail(result);

    return result.toString();
  }

  /**
   * Update the changed attribute in all formula O2M.
   *
   * @param creator
   * @param oldName
   * @param newName
   */
  protected void updateAttributeNameInFormulas(
      ConfiguratorCreator creator, String oldName, String newName) throws AxelorException {
    if (creator.getConfiguratorProductFormulaList() != null) {
      updateAttributeNameInFormulas(creator.getConfiguratorProductFormulaList(), oldName, newName);
    }
    if (creator.getConfiguratorSOLineFormulaList() != null) {
      updateAttributeNameInFormulas(creator.getConfiguratorSOLineFormulaList(), oldName, newName);
    }
  }

  /**
   * Update the changed attribute in formulas.
   *
   * @param formulas
   * @param oldAttributeName
   * @param newAttributeName
   */
  protected void updateAttributeNameInFormulas(
      List<? extends ConfiguratorFormula> formulas,
      String oldAttributeName,
      String newAttributeName) {

    formulas.forEach(
        configuratorFormula -> {
          if (!StringUtils.isEmpty(configuratorFormula.getFormula())) {
            configuratorFormula.setFormula(
                configuratorFormula.getFormula().replace(oldAttributeName, newAttributeName));
          }
        });
  }
}
