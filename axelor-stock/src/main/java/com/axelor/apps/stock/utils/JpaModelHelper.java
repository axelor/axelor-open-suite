/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.utils;

import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import jakarta.persistence.EntityManager;

public final class JpaModelHelper {

  private JpaModelHelper() {}

  /**
   * Ensure that the given model is attached to the current persistence context. If it's already
   * managed, returns it as-is, otherwise re-loads it by id.
   */
  public static <T extends Model> T ensureManaged(T model) {
    if (model == null || model.getId() == null) {
      return model;
    }
    EntityManager em = JPA.em();
    if (em.contains(model)) {
      return model;
    }
    Class<T> type = EntityHelper.getEntityClass(model);
    if (EntityHelper.isUninitialized(model)) {
      return em.getReference(type, model.getId());
    }
    return em.find(type, model.getId());
  }
}
