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
package com.axelor.apps.maintenance.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.apps.maintenance.service.PreventiveMaintenanceProcessService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

public class MaintenanceRequestRepo extends MaintenanceRequestRepository {

  @Override
  public MaintenanceRequest save(MaintenanceRequest entity) {

    boolean justCompleted = isJustCompleted(entity);
    if (justCompleted && entity.getDoneOn() == null) {
      entity.setDoneOn(Beans.get(AppBaseService.class).getTodayDate(null));
    }

    LocalDate doneOn = entity.getDoneOn();
    LocalDate expectedDate = entity.getExpectedDate();

    if (entity.getActionSelect() == ACTION_CORRECTIVE) {
      entity.setEndDate(doneOn != null ? doneOn.plusDays(1) : expectedDate.plusDays(1));
      entity.setStartDate(entity.getRequestDate());
    } else {
      entity.setStartDate(doneOn != null ? doneOn : expectedDate);
      entity.setEndDate(entity.getStartDate());
    }

    if (justCompleted) {
      Beans.get(PreventiveMaintenanceProcessService.class).updateEquipmentOnCompletion(entity);
    }

    return super.save(entity);
  }

  /**
   * Whether this save moves the request to completed, whatever the way the status was written
   * (button, kanban card move, generic REST write).
   */
  protected boolean isJustCompleted(MaintenanceRequest entity) {
    Integer status = entity.getStatusSelect();
    if (status == null || status != STATUS_COMPLETED) {
      return false;
    }
    Integer previousStatus = getCommittedStatus(entity.getId());
    return previousStatus == null || previousStatus != STATUS_COMPLETED;
  }

  /**
   * The status as last committed. It is read apart from the current session because the generic
   * REST save has already flushed the new value into the current transaction when it reaches the
   * repository.
   */
  protected Integer getCommittedStatus(Long id) {
    if (id == null) {
      return null;
    }
    EntityManager entityManager = JPA.em().getEntityManagerFactory().createEntityManager();
    try {
      List<Integer> result =
          entityManager
              .createQuery(
                  "select self.statusSelect from MaintenanceRequest self where self.id = :id",
                  Integer.class)
              .setParameter("id", id)
              .getResultList();
      return result.isEmpty() ? null : result.get(0);
    } finally {
      entityManager.close();
    }
  }

  @Override
  public MaintenanceRequest copy(MaintenanceRequest entity, boolean deep) {
    MaintenanceRequest copy = super.copy(entity, deep);
    copy.setStatusSelect(STATUS_PLANNED);
    return copy;
  }
}
