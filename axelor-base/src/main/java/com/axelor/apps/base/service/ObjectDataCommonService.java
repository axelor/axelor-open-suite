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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DataConfigLine;
import com.axelor.apps.base.db.repo.DataConfigLineRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.AdminExceptionMessage;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import java.util.Locale;
import java.util.ResourceBundle;

public class ObjectDataCommonService {

  private ObjectDataCommonService() {
    throw new IllegalStateException("Utility class");
  }

  public static ResourceBundle getResourceBundle(String language) {

    ResourceBundle bundle;

    if (language == null) {
      bundle = I18n.getBundle();
    } else if (language.equals("fr")) {
      bundle = I18n.getBundle(Locale.FRANCE);
    } else {
      bundle = I18n.getBundle(Locale.ENGLISH);
    }

    return bundle;
  }

  public static String getNameColumn(MetaField field) throws ClassNotFoundException {

    MetaModel metaModel = Beans.get(MetaModelRepository.class).findByName(field.getTypeName());

    return Mapper.of(findModelClass(metaModel)).getNameField().getName();
  }

  public static Class<? extends Model> findModelClass(MetaModel metaModel)
      throws ClassNotFoundException {

    Class<?> modelClass = Class.forName(metaModel.getFullName());

    return (Class<? extends Model>) modelClass;
  }

  public static Query<? extends Model> createQuery(
      Long recordId, DataConfigLine line, Class<? extends Model> modelClass)
      throws AxelorException {

    String filter;
    switch (line.getTypeSelect()) {
      case DataConfigLineRepository.TYPE_PATH:
        MetaField relationalField = line.getMetaFieldPath();
        if (relationalField == null) {
          throw new AxelorException(
              line,
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(AdminExceptionMessage.EMPTY_RELATIONAL_FIELD_IN_DATA_CONFIG_LINE),
              line.getMetaModel().getName());
        }
        filter = createFilter(relationalField.getName());
        break;

      case DataConfigLineRepository.TYPE_QUERY:
        filter = line.getPath();
        if (filter == null) {
          throw new AxelorException(
              line,
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(AdminExceptionMessage.EMPTY_QUERY_IN_DATA_CONFIG_LINE),
              line.getMetaModel().getName());
        }
        break;

      default:
        throw new AxelorException(
            line, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Unknown case");
    }

    return JpaRepository.of(modelClass).all().filter(filter, recordId);
  }

  public static String createFilter(String path) {

    if (path == null || path.equals("id")) {
      path = "id";
    } else if (!path.endsWith(".id")) {
      path = path + ".id";
    }

    return "self." + path + " = ?1";
  }
}
