package com.axelor.apps.stock.utils;

import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import javax.persistence.EntityManager;

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
