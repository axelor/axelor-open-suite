package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.repo.EquipementMaintenanceRepository;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.production.db.ProductionBatch;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
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