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
import com.axelor.apps.maintenance.db.repo.EquipementMaintenanceRepository;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.production.db.ProductionBatch;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PreventiveMaintenanceProcessServiceImpl
    implements PreventiveMaintenanceProcessService {

  protected final PreventiveMaintenanceCriterionService criterionService;
  protected final MaintenanceRequestCreateService maintenanceRequestCreateService;
  protected final EquipementMaintenanceRepository equipementMaintenanceRepository;
  protected final AppBaseService appBaseService;

  @Inject
  public PreventiveMaintenanceProcessServiceImpl(
      PreventiveMaintenanceCriterionService criterionService,
      MaintenanceRequestCreateService maintenanceRequestCreateService,
      EquipementMaintenanceRepository equipementMaintenanceRepository,
      AppBaseService appBaseService) {
    this.criterionService = criterionService;
    this.maintenanceRequestCreateService = maintenanceRequestCreateService;
    this.equipementMaintenanceRepository = equipementMaintenanceRepository;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public boolean processEquipment(EquipementMaintenance equipment, ProductionBatch productionBatch)
      throws AxelorException {
    if (!criterionService.shouldTriggerMaintenance(equipment)) {
      return false;
    }

    LocalDate previousNextMtnDate = equipment.getNextMtnDate();
    LocalDate today = appBaseService.getTodayDate(null);

    LocalDate expectedDate = computeExpectedDate(equipment, previousNextMtnDate, today);

    maintenanceRequestCreateService.createMaintenanceRequest(
        equipment, expectedDate, MaintenanceRequestRepository.ACTION_PREVENTIVE);

    updateNextMtnDate(equipment, previousNextMtnDate, today);
    updateLastMtnOperatingHoursRef(equipment);

    equipementMaintenanceRepository.save(equipment);
    return true;
  }

  protected LocalDate computeExpectedDate(
      EquipementMaintenance equipment, LocalDate previousNextMtnDate, LocalDate today) {
    if (previousNextMtnDate != null) {
      return previousNextMtnDate;
    }
    if (equipment.getMtnEachDay() > 0) {
      return today.plusDays(equipment.getMtnEachDay());
    }
    return today;
  }

  protected void updateNextMtnDate(
      EquipementMaintenance equipment, LocalDate previousNextMtnDate, LocalDate today) {
    if (equipment.getMtnEachDay() <= 0) {
      return;
    }
    if (previousNextMtnDate != null) {
      equipment.setNextMtnDate(previousNextMtnDate.plusDays(equipment.getMtnEachDay()));
    } else {
      equipment.setNextMtnDate(today.plusDays(equipment.getMtnEachDay()));
    }
  }

  protected void updateLastMtnOperatingHoursRef(EquipementMaintenance equipment) {
    if (equipment.getMtnEachDuration() <= 0 || equipment.getMachine() == null) {
      return;
    }
    BigDecimal machineHours = criterionService.getMachineOperatingHours(equipment.getMachine());
    equipment.setLastMtnOperatingHoursRef(machineHours);
  }
}