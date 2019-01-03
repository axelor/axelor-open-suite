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
package com.axelor.apps.tool;

import com.axelor.apps.tool.exception.IExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Column;

public final class ModelTool {

  private ModelTool() {}

  /**
   * Apply consumer to each record found from collection of IDs.
   *
   * @param ids collection of IDs.
   * @param consumer to apply on each record.
   * @return the number of errors that occurred.
   */
  public static <T extends Model> int apply(
      Class<? extends Model> modelClass,
      Collection<? extends Number> ids,
      ThrowConsumer<T> consumer) {

    Preconditions.checkNotNull(ids, I18n.get("The collection of IDs cannot be null."));
    Preconditions.checkNotNull(consumer, I18n.get("The consumer cannot be null."));

    int errorCount = 0;

    for (Number id : ids) {
      try {
        if (id != null) {
          Model model = JPA.find(modelClass, id.longValue());
          if (model != null) {
            consumer.accept((T) model);
            continue;
          }
        }

        throw new AxelorException(
            modelClass,
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get("Cannot find record #%s"),
            String.valueOf(id));
      } catch (Exception e) {
        ++errorCount;
        TraceBackService.trace(e);
      } finally {
        JPA.clear();
      }
    }

    return errorCount;
  }

  /**
   * Get unique constraint errors.
   *
   * @param model
   * @param messages
   * @return
   */
  public static Map<String, String> getUniqueErrors(Model model, Map<String, String> messages) {
    Map<String, String> errors = new HashMap<>();
    Collection<Field> fields = ModelTool.checkUniqueFields(model);

    for (Field field : fields) {
      String message =
          messages.getOrDefault(field.getName(), IExceptionMessage.RECORD_UNIQUE_FIELD);
      errors.put(field.getName(), message);
    }

    return errors;
  }

  /**
   * Get unique constraint errors.
   *
   * @param model
   * @return
   */
  public static Map<String, String> getUniqueErrors(Model model) {
    return getUniqueErrors(model, Collections.emptyMap());
  }

  /**
   * Get set of fields affected by unique constraint error.
   *
   * @param model
   * @return
   */
  private static Set<Field> checkUniqueFields(Model model) {
    Set<Field> errors = new HashSet<>();
    Class<? extends Model> modelClass = EntityHelper.getEntityClass(model);

    for (Field field : modelClass.getDeclaredFields()) {
      Column column = field.getAnnotation(Column.class);

      if (column == null || !column.unique()) {
        continue;
      }

      String filter = String.format("self.%s = :value", field.getName());
      String getterName = fieldNameToGetter(field.getName());

      try {
        Method getter = modelClass.getMethod(getterName);
        Object value = getter.invoke(model);
        Model existing = JPA.all(modelClass).filter(filter).bind("value", value).fetchOne();

        if (existing != null && !existing.getId().equals(model.getId())) {
          errors.add(field);
        }
      } catch (NoSuchMethodException
          | SecurityException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException e) {
        TraceBackService.trace(e);
      }
    }

    return errors;
  }

  private static String fieldNameToGetter(String name) {
    return "get" + capitalize(name);
  }

  private static String capitalize(String string) {
    return string.substring(0, 1).toUpperCase() + string.substring(1);
  }

  @SuppressWarnings("unchecked")
  public static <T> T toBean(Class<T> klass, Object mapObject) {
    Map<String, Object> map = (Map<String, Object>) mapObject;
    return Mapper.toBean(klass, map);
  }

  /**
   * Copy the content of a list with repository copy method.
   *
   * @param repo Repository to use for copy model.
   * @param src The source list to copy.
   * @param deep Copy all deep reference.
   * @param <T> The list model.
   * @return A new list with the content of src list.
   */
  public static <T extends Model> List<T> copy(JpaRepository<T> repo, List<T> src, boolean deep) {
    List<T> dest = new ArrayList<>();
    for (T obj : src) {
      T cpy = repo.copy(obj, deep);
      dest.add(cpy);
    }
    return dest;
  }
}
