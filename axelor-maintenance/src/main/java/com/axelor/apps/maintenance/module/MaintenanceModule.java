/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.maintenance.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.maintenance.db.repo.EquipementMaintenanceRepo;
import com.axelor.apps.maintenance.db.repo.EquipementMaintenanceRepository;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepo;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.maintenance.service.BillOfMaterialServiceMaintenanceImpl;
import com.axelor.apps.maintenance.service.MaintenanceRequestService;
import com.axelor.apps.maintenance.service.MaintenanceRequestServiceImpl;
import com.axelor.apps.maintenance.service.ManufOrderPrintServiceMaintenanceImpl;
import com.axelor.apps.maintenance.service.ManufOrderWorkFlowMaintenanceService;
import com.axelor.apps.maintenance.service.ProdProcessMaintenanceService;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.apps.production.service.ProdProcessService;
import com.axelor.apps.production.service.manuforder.ManufOrderPrintServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;

public class MaintenanceModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(EquipementMaintenanceRepository.class).to(EquipementMaintenanceRepo.class);
    bind(MaintenanceRequestService.class).to(MaintenanceRequestServiceImpl.class);
    bind(MaintenanceRequestRepository.class).to(MaintenanceRequestRepo.class);
    bind(BillOfMaterialServiceImpl.class).to(BillOfMaterialServiceMaintenanceImpl.class);
    bind(ManufOrderWorkflowService.class).to(ManufOrderWorkFlowMaintenanceService.class);
    bind(ManufOrderPrintServiceImpl.class).to(ManufOrderPrintServiceMaintenanceImpl.class);
    bind(ProdProcessService.class).to(ProdProcessMaintenanceService.class);
  }
}
