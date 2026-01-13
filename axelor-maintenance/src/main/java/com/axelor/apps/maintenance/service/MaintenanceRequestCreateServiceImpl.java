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
