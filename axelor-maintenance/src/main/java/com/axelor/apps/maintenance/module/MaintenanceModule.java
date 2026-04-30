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
package com.axelor.apps.maintenance.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.maintenance.db.repo.EquipementMaintenanceRepo;
import com.axelor.apps.maintenance.db.repo.EquipementMaintenanceRepository;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepo;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.maintenance.service.BillOfMaterialComputeNameServiceMaintenanceImpl;
import com.axelor.apps.maintenance.service.BillOfMaterialMaintenanceService;
import com.axelor.apps.maintenance.service.BillOfMaterialServiceMaintenanceImpl;
import com.axelor.apps.maintenance.service.CostSheetServiceMaintenanceImpl;
import com.axelor.apps.maintenance.service.MaintenanceRequestCreateService;
import com.axelor.apps.maintenance.service.MaintenanceRequestCreateServiceImpl;
import com.axelor.apps.maintenance.service.MaintenanceRequestInitValueService;
import com.axelor.apps.maintenance.service.MaintenanceRequestInitValueServiceImpl;
import com.axelor.apps.maintenance.service.MaintenanceRequestService;
import com.axelor.apps.maintenance.service.MaintenanceRequestServiceImpl;
import com.axelor.apps.maintenance.service.ManufOrderPlanServiceMaintenanceImpl;
import com.axelor.apps.maintenance.service.ManufOrderPrintService;
import com.axelor.apps.maintenance.service.ManufOrderPrintServiceImpl;
import com.axelor.apps.maintenance.service.ManufOrderStockMoveServiceMaintenanceImpl;
import com.axelor.apps.maintenance.service.ManufOrderWorkflowMaintenanceServiceImpl;
import com.axelor.apps.maintenance.service.OperationOrderStockMoveServiceMaintenanceImpl;
import com.axelor.apps.production.service.BillOfMaterialComputeNameServiceImpl;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.apps.production.service.costsheet.CostSheetServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderPlanServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveServiceImpl;

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
    bind(BillOfMaterialComputeNameServiceImpl.class)
        .to(BillOfMaterialComputeNameServiceMaintenanceImpl.class);
    bind(ManufOrderPlanServiceImpl.class).to(ManufOrderPlanServiceMaintenanceImpl.class);
    bind(MaintenanceRequestInitValueService.class).to(MaintenanceRequestInitValueServiceImpl.class);
    bind(MaintenanceRequestCreateService.class).to(MaintenanceRequestCreateServiceImpl.class);

    // Maintenance stock move and cost sheet overrides
    bind(CostSheetServiceImpl.class).to(CostSheetServiceMaintenanceImpl.class);
    bind(ManufOrderStockMoveServiceImpl.class).to(ManufOrderStockMoveServiceMaintenanceImpl.class);
    bind(OperationOrderStockMoveServiceImpl.class)
        .to(OperationOrderStockMoveServiceMaintenanceImpl.class);
  }
}
