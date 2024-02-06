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
package com.axelor.apps.businessproduction.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproduction.db.repo.ManufOrderBusinessProductionManagementRepository;
import com.axelor.apps.businessproduction.service.CostSheetServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.InvoicingProjectServiceBusinessProdImpl;
import com.axelor.apps.businessproduction.service.ManufOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ManufOrderValidateBusinessService;
import com.axelor.apps.businessproduction.service.ManufOrderValidateBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.ManufacturingOperationBusinessProductionCheckService;
import com.axelor.apps.businessproduction.service.ManufacturingOperationBusinessProductionCheckServiceImpl;
import com.axelor.apps.businessproduction.service.ManufacturingOperationServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ManufacturingOperationTimesheetService;
import com.axelor.apps.businessproduction.service.ManufacturingOperationTimesheetServiceImpl;
import com.axelor.apps.businessproduction.service.ManufacturingOperationValidateBusinessService;
import com.axelor.apps.businessproduction.service.ManufacturingOperationValidateBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.ManufacturingOperationWorkflowBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.ProductionOrderSaleOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ProductionOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ProductionOrderWizardServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.SaleOrderLineBusinessProductionServiceImpl;
import com.axelor.apps.businessproduction.service.SaleOrderWorkflowServiceBusinessProductionImpl;
import com.axelor.apps.businessproduction.service.TimesheetBusinessProductionWorkflowServiceImpl;
import com.axelor.apps.businessproduction.service.TimesheetLineBusinessProductionService;
import com.axelor.apps.businessproduction.service.TimesheetLineBusinessProductionServiceImpl;
import com.axelor.apps.businessproduction.service.TimesheetLineCreateBusinessService;
import com.axelor.apps.businessproduction.service.TimesheetLineCreateBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.TimesheetLineUpdateBusinessService;
import com.axelor.apps.businessproduction.service.TimesheetLineUpdateBusinessServiceImpl;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.businessproject.service.SaleOrderLineProjectServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetLineUpdateServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetWorkflowServiceImpl;
import com.axelor.apps.production.db.repo.ManufOrderManagementRepository;
import com.axelor.apps.production.service.SaleOrderWorkflowServiceProductionImpl;
import com.axelor.apps.production.service.costsheet.CostSheetServiceImpl;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationServiceImpl;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationWorkflowServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderWizardServiceImpl;

public class BusinessProductionModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ProductionOrderServiceImpl.class).to(ProductionOrderServiceBusinessImpl.class);
    bind(CostSheetServiceImpl.class).to(CostSheetServiceBusinessImpl.class);
    bind(ManufOrderServiceImpl.class).to(ManufOrderServiceBusinessImpl.class);
    bind(ManufacturingOperationServiceImpl.class)
        .to(ManufacturingOperationServiceBusinessImpl.class);
    bind(ProductionOrderServiceImpl.class).to(ProductionOrderServiceBusinessImpl.class);
    bind(ProductionOrderWizardServiceImpl.class).to(ProductionOrderWizardServiceBusinessImpl.class);
    bind(ProductionOrderSaleOrderServiceImpl.class)
        .to(ProductionOrderSaleOrderServiceBusinessImpl.class);
    bind(InvoicingProjectService.class).to(InvoicingProjectServiceBusinessProdImpl.class);
    bind(ManufOrderValidateBusinessService.class).to(ManufOrderValidateBusinessServiceImpl.class);
    bind(ManufacturingOperationValidateBusinessService.class)
        .to(ManufacturingOperationValidateBusinessServiceImpl.class);
    bind(TimesheetWorkflowServiceImpl.class)
        .to(TimesheetBusinessProductionWorkflowServiceImpl.class);
    bind(ManufacturingOperationTimesheetService.class)
        .to(ManufacturingOperationTimesheetServiceImpl.class);
    bind(ManufOrderManagementRepository.class)
        .to(ManufOrderBusinessProductionManagementRepository.class);
    bind(SaleOrderWorkflowServiceProductionImpl.class)
        .to(SaleOrderWorkflowServiceBusinessProductionImpl.class);
    bind(SaleOrderLineProjectServiceImpl.class)
        .to(SaleOrderLineBusinessProductionServiceImpl.class);
    bind(ManufacturingOperationWorkflowServiceImpl.class)
        .to(ManufacturingOperationWorkflowBusinessServiceImpl.class);
    bind(TimesheetLineBusinessProductionService.class)
        .to(TimesheetLineBusinessProductionServiceImpl.class);

    bind(ManufacturingOperationBusinessProductionCheckService.class)
        .to(ManufacturingOperationBusinessProductionCheckServiceImpl.class);
    bind(TimesheetLineCreateBusinessService.class).to(TimesheetLineCreateBusinessServiceImpl.class);
    bind(TimesheetLineUpdateBusinessService.class).to(TimesheetLineUpdateBusinessServiceImpl.class);
    bind(TimesheetLineUpdateServiceImpl.class).to(TimesheetLineUpdateBusinessServiceImpl.class);
  }
}
