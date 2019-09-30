/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.DataConfigLine;
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
      Long recordId, DataConfigLine line, Class<? extends Model> modelClass) {

    Query<? extends Model> query = JpaRepository.of(modelClass).all();
    String filter = line.getTypeSelect() == 0 ? createFilter(line.getPath()) : line.getPath();
    query.filter(filter, recordId);

    return query;
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
