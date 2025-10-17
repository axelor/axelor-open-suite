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
package com.axelor.apps.maintenance.rest.dto;

import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class MaintenanceRequestPostRequest extends RequestPostStructure {

  @NotNull
  @Min(0)
  private Long equipementMaintenanceId;

  @NotNull private LocalDate expectedDate;

  @Min(MaintenanceRequestRepository.ACTION_CORRECTIVE)
  @Max(MaintenanceRequestRepository.ACTION_PREVENTIVE)
  private int actionSelect;

  public Long getEquipementMaintenanceId() {
    return equipementMaintenanceId;
  }

  public void setEquipementMaintenanceId(Long equipementMaintenanceId) {
    this.equipementMaintenanceId = equipementMaintenanceId;
  }

  public LocalDate getExpectedDate() {
    return expectedDate;
  }

  public void setExpectedDate(LocalDate expectedDate) {
    this.expectedDate = expectedDate;
  }

  public int getActionSelect() {
    return actionSelect;
  }

  public void setActionSelect(int actionSelect) {
    this.actionSelect = actionSelect;
  }

  public EquipementMaintenance fetchEquipmentMaintenance() {
    if (equipementMaintenanceId == null || equipementMaintenanceId == 0L) {
      return null;
    }
    return ObjectFinder.find(
        EquipementMaintenance.class, equipementMaintenanceId, ObjectFinder.NO_VERSION);
  }
}
