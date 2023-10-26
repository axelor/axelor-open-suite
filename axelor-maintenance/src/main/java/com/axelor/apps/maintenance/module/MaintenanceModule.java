/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.maintenance.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.maintenance.db.repo.EquipementMaintenanceRepo;
import com.axelor.apps.maintenance.db.repo.EquipementMaintenanceRepository;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepo;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.maintenance.service.BillOfMaterialMaintenanceService;
import com.axelor.apps.maintenance.service.BillOfMaterialServiceMaintenanceImpl;
import com.axelor.apps.maintenance.service.MaintenanceRequestService;
import com.axelor.apps.maintenance.service.MaintenanceRequestServiceImpl;
import com.axelor.apps.maintenance.service.ManufOrderPrintService;
import com.axelor.apps.maintenance.service.ManufOrderPrintServiceImpl;
import com.axelor.apps.maintenance.service.ManufOrderWorkflowMaintenanceServiceImpl;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowServiceImpl;

public class MaintenanceModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(EquipementMaintenanceRepository.class).to(EquipementMaintenanceRepo.class);
    bind(MaintenanceRequestService.class).to(MaintenanceRequestServiceImpl.class);
    bind(MaintenanceRequestRepository.class).to(MaintenanceRequestRepo.class);
    bind(BillOfMaterialMaintenanceService.class).to(BillOfMaterialServiceMaintenanceImpl.class);
    bind(BillOfMaterialServiceImpl.class).to(BillOfMaterialServiceMaintenanceImpl.class);
    bind(ManufOrderWorkflowServiceImpl.class).to(ManufOrderWorkflowMaintenanceServiceImpl.class);
    bind(ManufOrderPrintService.class).to(ManufOrderPrintServiceImpl.class);
  }
}
