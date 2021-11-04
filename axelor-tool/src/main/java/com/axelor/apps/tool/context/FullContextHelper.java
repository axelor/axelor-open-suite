/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.context;

import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.JpaScanner;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaJsonRecordRepository;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FullContextHelper {

  @SuppressWarnings("unchecked")
  public static FullContext create(String modelName, Map<String, Object> values) {
    if (JpaScanner.findModel(modelName) == null) {
      return new FullContext(Beans.get(MetaJsonRecordRepository.class).create(modelName, values));
    }
    return new FullContext(getRepository(modelName).create(values));
  }

  public static FullContext create(Model model) {
    return new FullContext(model);
  }

  public static FullContext filterOne(String modelName, String queryStr, Object... params)
      throws AxelorException {

    Query<? extends Model> query = createQuery(modelName, queryStr, null, params);
    try {
      Model model = query.fetchOne();
      if (model != null) {
        return new FullContext(model);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get("Error executing query: %s"),
          queryStr);
    }

    return null;
  }

  public static FullContext filterOne(String modelName, String queryStr) throws AxelorException {

    Query<? extends Model> query = createQuery(modelName, queryStr, null, null);
    try {
      Model model = query.fetchOne();
      if (model != null) {
        return new FullContext(model);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get("Error executing query: %s"),
          queryStr);
    }

    return null;
  }

  public static FullContext filterOne(
      String modelName, String queryStr, Map<String, Object> paramMap) throws AxelorException {

    Query<? extends Model> query = createQuery(modelName, queryStr, paramMap, null);
    try {
      Model model = query.fetchOne();
      if (model != null) {
        return new FullContext(model);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get("Error executing query: %s"),
          queryStr);
    }

    return null;
  }

  private static Query<? extends Model> createQuery(
      String modelName, String queryStr, Map<String, Object> paramMap, Object[] params) {

    JpaRepository<? extends Model> repo = null;
    if (JpaScanner.findModel(modelName) == null) {
      repo = Beans.get(MetaJsonRecordRepository.class);
      if (queryStr != null) {
        queryStr += " AND self.jsonModel = '" + modelName + "'";
      } else {
        queryStr = "self.jsonModel = '" + modelName + "'";
      }
    } else {
      repo = getRepository(modelName);
    }

    Query<? extends Model> query = repo.all();
    if (paramMap != null) {
      query = query.filter(queryStr).bind(paramMap);
    } else if (params != null) {
      query = query.filter(queryStr, params);
    } else if (queryStr != null) {
      query = query.filter(queryStr);
    }

    return query;
  }

  public static List<FullContext> filter(String modelName, String queryStr) {

    List<FullContext> wkfEntities = new ArrayList<FullContext>();

    Query<? extends Model> query = createQuery(modelName, queryStr, null, null);

    for (Model model : query.fetch()) {
      wkfEntities.add(new FullContext(model));
    }

    return wkfEntities;
  }

  public static List<FullContext> filter(String modelName, String queryStr, Object... params) {

    List<FullContext> wkfEntities = new ArrayList<FullContext>();

    if (params == null) {
      params = new Object[] {null};
    }

    Query<? extends Model> query = createQuery(modelName, queryStr, null, params);

    for (Model model : query.fetch()) {
      wkfEntities.add(new FullContext(model));
    }

    return wkfEntities;
  }

  public static List<FullContext> filter(
      String modelName, String queryStr, Map<String, Object> paramMap) {

    List<FullContext> wkfEntities = new ArrayList<FullContext>();

    Query<? extends Model> query = createQuery(modelName, queryStr, paramMap, null);

    for (Model model : query.fetch()) {
      wkfEntities.add(new FullContext(model));
    }

    return wkfEntities;
  }

  public static FullContext create(String modelName) {
    return create(modelName, new HashMap<String, Object>());
  }

  @Transactional
  public static FullContext save(Object object) {

    Model model = null;

    if (object instanceof Model) {
      model = (Model) model;
    } else if (object instanceof FullContext) {
      model = (Model) ((FullContext) object).getTarget();
    }

    if (model != null) {
      boolean active = JPA.em().getTransaction().isActive();

      if (!active) {
        JPA.em().getTransaction().begin();
      }
      FullContext wkfContext =
          new FullContext(JpaRepository.of(EntityHelper.getEntityClass(model)).save(model));

      if (!active) {
        JPA.em().getTransaction().commit();
      }

      return wkfContext;
    }

    return null;
  }

  public static JpaRepository<? extends Model> getRepository(String modelName) {

    return JpaRepository.of((Class<Model>) JpaScanner.findModel(modelName));
  }

  public static FullContext find(String modelName, Object recordId) throws AxelorException {

    if (recordId == null || !(recordId instanceof Long)) {
      return null;
    }

    return filterOne(modelName, "self.id = ?1", (Long) recordId);
  }
}
