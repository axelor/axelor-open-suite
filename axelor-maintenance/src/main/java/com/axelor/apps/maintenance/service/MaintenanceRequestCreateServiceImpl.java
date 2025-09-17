package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.maintenance.exception.MaintenanceExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.time.LocalDate;

public class MaintenanceRequestCreateServiceImpl implements MaintenanceRequestCreateService {

  protected final MaintenanceRequestInitValueService maintenanceRequestInitValueService;
  protected final MaintenanceRequestRepository maintenanceRequestRepository;

  @Inject
  public MaintenanceRequestCreateServiceImpl(
      MaintenanceRequestInitValueService maintenanceRequestInitValueService,
      MaintenanceRequestRepository maintenanceRequestRepository) {
    this.maintenanceRequestInitValueService = maintenanceRequestInitValueService;
    this.maintenanceRequestRepository = maintenanceRequestRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public MaintenanceRequest createMaintenanceRequest(
      EquipementMaintenance equipementMaintenance, LocalDate expectedDate, int actionSelect)
      throws AxelorException {

    if (equipementMaintenance == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(MaintenanceExceptionMessage.MAINTENANCE_REQUEST_CREATION_EQUIPMENT_MISSING));
    }

    if (expectedDate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(MaintenanceExceptionMessage.MAINTENANCE_REQUEST_CREATION_EXPECTED_DATE_MISSING));
    }

    MaintenanceRequest maintenanceRequest = new MaintenanceRequest();

    maintenanceRequestInitValueService.getDefaultValues(maintenanceRequest);
    maintenanceRequest.setEquipementMaintenance(equipementMaintenance);
    maintenanceRequest.setExpectedDate(expectedDate);
    maintenanceRequest.setActionSelect(actionSelect);
    maintenanceRequest.setMachine(equipementMaintenance.getMachine());
    return maintenanceRequestRepository.save(maintenanceRequest);
  }
}
