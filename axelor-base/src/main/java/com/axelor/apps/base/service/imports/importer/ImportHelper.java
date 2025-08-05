/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.imports.importer;

import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaSelectItem;
import com.google.api.client.util.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public final class ImportHelper {

  public static boolean isValuePresent(Object value) {
    if (value == null) {
      return false;
    }
    if (value instanceof String) {
      return !Strings.isNullOrEmpty((String) value);
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  public static boolean isRecordPresent(String entityName, String fieldNames, Object... values) {
    if (!isValidInput(entityName, fieldNames, values)) {
      return false;
    }
    try {
      Class<? extends Model> entityClass = (Class<? extends Model>) Class.forName(entityName);
      String filter = buildFilter(fieldNames);
      return JPA.all(entityClass).filter(filter, values).fetchOne() != null;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static boolean checkUniqueConstraint(
      String entityName, String searchFieldNames, Object... args) {
    String[] searchFields = splitFieldNames(searchFieldNames);
    int searchFieldLength = searchFields.length;

    if (args.length < searchFieldLength + 1) {
      return false;
    }
    Object[] searchFieldValues = Arrays.copyOfRange(args, 0, searchFieldLength);
    String fieldNames = (String) args[searchFieldLength];
    String[] fields = splitFieldNames(fieldNames);
    int fieldLength = fields.length;

    if (args.length < searchFieldLength + 1 + fieldLength) {
      return false;
    }
    Object[] values =
        Arrays.copyOfRange(args, searchFieldLength + 1, searchFieldLength + 1 + fieldLength);

    return checkUniqueConstraint(
        entityName, searchFieldNames, searchFieldValues, fieldNames, values);
  }

  @SuppressWarnings("unchecked")
  protected static boolean checkUniqueConstraint(
      String entityName,
      String searchFieldNames,
      Object[] searchFieldValues,
      String fieldNames,
      Object[] values) {
    if (!isValidInput(entityName, searchFieldNames, searchFieldValues)
        || !isValidInput(entityName, fieldNames, values)) {
      return false;
    }
    try {
      Class<? extends Model> entityClass = (Class<? extends Model>) Class.forName(entityName);
      String searchFilter = buildFilter(searchFieldNames);
      StringBuilder filter = new StringBuilder(buildFilter(fieldNames));
      List<Object> params = new ArrayList<>(Arrays.asList(values));

      Model record = JPA.all(entityClass).filter(searchFilter, searchFieldValues).fetchOne();
      if (record != null) {
        filter.append(" AND self.id != ?").append(params.size() + 1);
        params.add(record.getId());
      }
      return JPA.all(entityClass).filter(filter.toString(), params.toArray()).fetchOne() == null;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  public static boolean checkSelection(String entityName, String fieldName, Object value) {
    if (Strings.isNullOrEmpty(entityName) || Strings.isNullOrEmpty(fieldName)) {
      return false;
    }
    try {
      Class<? extends Model> entityClass = (Class<? extends Model>) Class.forName(entityName);
      Property property = Mapper.of(entityClass).getProperty(fieldName);
      if (property == null) {
        return false;
      }
      if (!isValuePresent(value)) {
        return !property.isRequired();
      }
      String selectionName = property.getSelection();
      if (Strings.isNullOrEmpty(selectionName)) {
        return false;
      }
      List<MetaSelectItem> items =
          JPA.all(MetaSelectItem.class).filter("self.select.name = ?", selectionName).fetch();
      if (CollectionUtils.isEmpty(items)) {
        return false;
      }
      Set<String> validValues =
          items.stream()
              .map(MetaSelectItem::getValue)
              .filter(Objects::nonNull)
              .collect(Collectors.toSet());

      return Arrays.stream(String.valueOf(value).split(",")).allMatch(validValues::contains);
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  protected static boolean isValidInput(String entityName, String fieldNames, Object[] values) {
    if (Strings.isNullOrEmpty(entityName) || Strings.isNullOrEmpty(fieldNames) || values == null) {
      return false;
    }
    String[] fields = splitFieldNames(fieldNames);
    return fields.length == values.length;
  }

  protected static String buildFilter(String fieldNames) {
    String[] fields = splitFieldNames(fieldNames);
    StringJoiner joiner = new StringJoiner(" AND ");
    for (int i = 0; i < fields.length; i++) {
      joiner.add("self." + fields[i] + " = ?" + (i + 1));
    }
    return joiner.toString();
  }

  protected static String[] splitFieldNames(String fieldNames) {
    return fieldNames.trim().split("\\s*,\\s*");
  }
}
