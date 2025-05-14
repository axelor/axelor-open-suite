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
package com.axelor.apps.businessproduction.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproduction.db.repo.ManufOrderBusinessProductionManagementRepository;
import com.axelor.apps.businessproduction.observer.SaleOrderBusinessProdObserver;
import com.axelor.apps.businessproduction.service.BusinessProjectProdOrderService;
import com.axelor.apps.businessproduction.service.BusinessProjectProdOrderServiceImpl;
import com.axelor.apps.businessproduction.service.CostSheetServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.InvoicingProjectServiceBusinessProdImpl;
import com.axelor.apps.businessproduction.service.ManufOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ManufOrderValidateBusinessService;
import com.axelor.apps.businessproduction.service.ManufOrderValidateBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.OperationOrderBusinessProductionCheckService;
import com.axelor.apps.businessproduction.service.OperationOrderBusinessProductionCheckServiceImpl;
import com.axelor.apps.businessproduction.service.OperationOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.OperationOrderTimesheetService;
import com.axelor.apps.businessproduction.service.OperationOrderTimesheetServiceImpl;
import com.axelor.apps.businessproduction.service.OperationOrderValidateBusinessService;
import com.axelor.apps.businessproduction.service.OperationOrderValidateBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.OperationOrderWorkflowBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.ProductionOrderSaleOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ProductionOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ProductionOrderWizardServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.SaleOrderLineBomSyncServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.SaleOrderLineBusinessProductionServiceImpl;
import com.axelor.apps.businessproduction.service.SaleOrderLineDetailsBomSyncBusinessProductionServiceImpl;
import com.axelor.apps.businessproduction.service.SaleOrderLineDetailsBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.SaleOrderLineInitValueBusinessProductionServiceImpl;
import com.axelor.apps.businessproduction.service.SaleOrderProductionSyncBusinessService;
import com.axelor.apps.businessproduction.service.SaleOrderProductionSyncBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.SaleOrderServiceBusinessProductionImpl;
import com.axelor.apps.businessproduction.service.SolDetailsBusinessProductionService;
import com.axelor.apps.businessproduction.service.SolDetailsBusinessProductionServiceImpl;
import com.axelor.apps.businessproduction.service.SolDetailsRemoveBusinessProductionService;
import com.axelor.apps.businessproduction.service.SolDetailsRemoveBusinessProductionServiceImpl;
import com.axelor.apps.businessproduction.service.TimesheetBusinessProductionWorkflowServiceImpl;
import com.axelor.apps.businessproduction.service.TimesheetLineBusinessProductionService;
import com.axelor.apps.businessproduction.service.TimesheetLineBusinessProductionServiceImpl;
import com.axelor.apps.businessproduction.service.TimesheetLineCreateBusinessService;
import com.axelor.apps.businessproduction.service.TimesheetLineCreateBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.TimesheetLineRemoveBusinessProductionServiceImpl;
import com.axelor.apps.businessproduction.service.TimesheetLineUpdateBusinessService;
import com.axelor.apps.businessproduction.service.TimesheetLineUpdateBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.saleorder.status.SaleOrderWorkflowServiceBusinessProductionImpl;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.businessproject.service.SaleOrderLineInitValueProjectServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetLineRemoveServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetLineUpdateServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetWorkflowServiceImpl;
import com.axelor.apps.production.db.repo.ManufOrderManagementRepository;
import com.axelor.apps.production.service.SaleOrderLineBomSyncServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineDetailsBomSyncServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineDetailsServiceImpl;
import com.axelor.apps.production.service.costsheet.CostSheetServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderWizardServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.status.SaleOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineCreateSupplychainServiceImpl;

public class BusinessProductionModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ProductionOrderServiceImpl.class).to(ProductionOrderServiceBusinessImpl.class);
    bind(CostSheetServiceImpl.class).to(CostSheetServiceBusinessImpl.class);
    bind(ManufOrderServiceImpl.class).to(ManufOrderServiceBusinessImpl.class);
    bind(OperationOrderServiceImpl.class).to(OperationOrderServiceBusinessImpl.class);
    bind(ProductionOrderServiceImpl.class).to(ProductionOrderServiceBusinessImpl.class);
    bind(ProductionOrderWizardServiceImpl.class).to(ProductionOrderWizardServiceBusinessImpl.class);
    bind(ProductionOrderSaleOrderServiceImpl.class)
        .to(ProductionOrderSaleOrderServiceBusinessImpl.class);
    bind(InvoicingProjectService.class).to(InvoicingProjectServiceBusinessProdImpl.class);
    bind(ManufOrderValidateBusinessService.class).to(ManufOrderValidateBusinessServiceImpl.class);
    bind(OperationOrderValidateBusinessService.class)
        .to(OperationOrderValidateBusinessServiceImpl.class);
    bind(TimesheetWorkflowServiceImpl.class)
        .to(TimesheetBusinessProductionWorkflowServiceImpl.class);
    bind(OperationOrderTimesheetService.class).to(OperationOrderTimesheetServiceImpl.class);
    bind(ManufOrderManagementRepository.class)
        .to(ManufOrderBusinessProductionManagementRepository.class);
    bind(SaleOrderWorkflowServiceSupplychainImpl.class)
        .to(SaleOrderWorkflowServiceBusinessProductionImpl.class);
    bind(SaleOrderLineCreateSupplychainServiceImpl.class)
        .to(SaleOrderLineBusinessProductionServiceImpl.class);
    bind(OperationOrderWorkflowServiceImpl.class)
        .to(OperationOrderWorkflowBusinessServiceImpl.class);
    bind(TimesheetLineBusinessProductionService.class)
        .to(TimesheetLineBusinessProductionServiceImpl.class);

    bind(OperationOrderBusinessProductionCheckService.class)
        .to(OperationOrderBusinessProductionCheckServiceImpl.class);
    bind(TimesheetLineCreateBusinessService.class).to(TimesheetLineCreateBusinessServiceImpl.class);
    bind(TimesheetLineUpdateBusinessService.class).to(TimesheetLineUpdateBusinessServiceImpl.class);
    bind(TimesheetLineUpdateServiceImpl.class).to(TimesheetLineUpdateBusinessServiceImpl.class);
    bind(TimesheetLineRemoveServiceImpl.class)
        .to(TimesheetLineRemoveBusinessProductionServiceImpl.class);
    bind(BusinessProjectProdOrderService.class).to(BusinessProjectProdOrderServiceImpl.class);
    bind(SaleOrderBusinessProdObserver.class);
    bind(SolDetailsBusinessProductionService.class)
        .to(SolDetailsBusinessProductionServiceImpl.class);
    bind(SaleOrderLineDetailsBomSyncServiceImpl.class)
        .to(SaleOrderLineDetailsBomSyncBusinessProductionServiceImpl.class);
    bind(SaleOrderServiceSupplychainImpl.class).to(SaleOrderServiceBusinessProductionImpl.class);
    bind(SaleOrderProductionSyncBusinessService.class)
        .to(SaleOrderProductionSyncBusinessServiceImpl.class);
    bind(SaleOrderLineBomSyncServiceImpl.class).to(SaleOrderLineBomSyncServiceBusinessImpl.class);
    bind(SaleOrderLineInitValueProjectServiceImpl.class)
        .to(SaleOrderLineInitValueBusinessProductionServiceImpl.class);
    bind(SolDetailsRemoveBusinessProductionService.class)
        .to(SolDetailsRemoveBusinessProductionServiceImpl.class);
    bind(SaleOrderLineDetailsServiceImpl.class).to(SaleOrderLineDetailsBusinessServiceImpl.class);
  }
}
