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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.production.db.Machine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class PreventiveMaintenanceCriterionServiceImpl
    implements PreventiveMaintenanceCriterionService {

  protected static final BigDecimal SECONDS_PER_HOUR = new BigDecimal(3600);
  protected final AppBaseService appBaseService;
  protected final MaintenanceRequestRepository maintenanceRequestRepository;

  @Inject
  public PreventiveMaintenanceCriterionServiceImpl(
      AppBaseService appBaseService, MaintenanceRequestRepository maintenanceRequestRepository) {
    this.appBaseService = appBaseService;
    this.maintenanceRequestRepository = maintenanceRequestRepository;
  }

  @Override
  public Boolean evaluateCalendarCriterion(EquipementMaintenance equipment) throws AxelorException {
    if (equipment.getMtnEachDay() <= 0) {
      return null;
    }

    LocalDate today = appBaseService.getTodayDate(null);

    if (equipment.getNextMtnDate() != null) {
      return !today.isBefore(equipment.getNextMtnDate());
    }

    MaintenanceRequest lastCompleted = findLastCompletedPreventiveRequest(equipment);

    if (lastCompleted != null && lastCompleted.getDoneOn() != null) {
      long daysSinceLast = ChronoUnit.DAYS.between(lastCompleted.getDoneOn(), today);
      return daysSinceLast > equipment.getMtnEachDay();
    }

    return true;
  }

  @Override
  public Boolean evaluateOperatingHoursCriterion(EquipementMaintenance equipment)
      throws AxelorException {
    if (equipment.getMtnEachDuration() <= 0 || equipment.getMachine() == null) {
      return null;
    }

    BigDecimal machineHours = getMachineOperatingHours(equipment.getMachine());

    if (machineHours.signum() == 0) {
      return false;
    }

    BigDecimal lastRef = equipment.getLastMtnOperatingHoursRef();

    if (lastRef == null || lastRef.signum() == 0) {
      return true;
    }

    BigDecimal accumulated = machineHours.subtract(lastRef);
    return accumulated.compareTo(BigDecimal.valueOf(equipment.getMtnEachDuration())) >= 0;
  }

  @Override
  public boolean shouldTriggerMaintenance(EquipementMaintenance equipment) throws AxelorException {
    Boolean calendarResult = evaluateCalendarCriterion(equipment);
    Boolean hoursResult = evaluateOperatingHoursCriterion(equipment);

    List<Boolean> configuredResults = new ArrayList<>();
    if (calendarResult != null) {
      configuredResults.add(calendarResult);
    }
    if (hoursResult != null) {
      configuredResults.add(hoursResult);
    }

    if (configuredResults.isEmpty()) {
      return false;
    }

    if (equipment.getCreateMtnRequestSelect() == 1) {
      return configuredResults.stream().allMatch(Boolean::booleanValue);
    }

    return configuredResults.stream().anyMatch(Boolean::booleanValue);
  }

  protected MaintenanceRequest findLastCompletedPreventiveRequest(EquipementMaintenance equipment) {
    return maintenanceRequestRepository
        .all()
        .filter(
            "self.equipementMaintenance = :equipment"
                + " AND self.actionSelect = :preventive"
                + " AND self.statusSelect = :completed"
                + " AND self.doneOn IS NOT NULL")
        .bind("equipment", equipment)
        .bind("preventive", MaintenanceRequestRepository.ACTION_PREVENTIVE)
        .bind("completed", MaintenanceRequestRepository.STATUS_COMPLETED)
        .order("-doneOn")
        .fetchOne();
  }

  @Override
  public BigDecimal getMachineOperatingHours(Machine machine) {
    return BigDecimal.valueOf(machine.getOperatingDuration())
        .divide(SECONDS_PER_HOUR, 2, RoundingMode.HALF_UP);
  }
}
