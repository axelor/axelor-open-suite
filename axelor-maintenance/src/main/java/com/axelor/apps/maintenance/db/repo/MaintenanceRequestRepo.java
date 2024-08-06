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
package com.axelor.apps.maintenance.db.repo;

import com.axelor.apps.maintenance.db.MaintenanceRequest;
import java.time.LocalDate;

public class MaintenanceRequestRepo extends MaintenanceRequestRepository {

  @Override
  public MaintenanceRequest save(MaintenanceRequest entity) {

    LocalDate doneOn = entity.getDoneOn();
    LocalDate expectedDate = entity.getExpectedDate();

    if (entity.getActionSelect() == ACTION_CORRECTIVE) {
      entity.setEndDate(doneOn != null ? doneOn.plusDays(1) : expectedDate.plusDays(1));
      entity.setStartDate(entity.getRequestDate());
    } else {
      entity.setStartDate(doneOn != null ? doneOn : expectedDate);
      entity.setEndDate(entity.getStartDate());
    }

    return super.save(entity);
  }

  @Override
  public MaintenanceRequest copy(MaintenanceRequest entity, boolean deep) {
    MaintenanceRequest copy = super.copy(entity, deep);
    copy.setStatusSelect(STATUS_PLANNED);
    return copy;
  }
}
