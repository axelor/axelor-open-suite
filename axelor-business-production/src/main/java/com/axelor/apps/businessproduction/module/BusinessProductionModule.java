/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproduction.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproduction.service.CostSheetServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.InvoicingProjectServiceBusinessProdImpl;
import com.axelor.apps.businessproduction.service.ManufOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ManufOrderValidateBusinessService;
import com.axelor.apps.businessproduction.service.ManufOrderValidateBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.OperationOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.OperationOrderTimesheetService;
import com.axelor.apps.businessproduction.service.OperationOrderTimesheetServiceImpl;
import com.axelor.apps.businessproduction.service.OperationOrderValidateBusinessService;
import com.axelor.apps.businessproduction.service.OperationOrderValidateBusinessServiceImpl;
import com.axelor.apps.businessproduction.service.OperationOrderWorkflowServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ProductionOrderSaleOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ProductionOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ProductionOrderWizardServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.TimesheetBusinessProductionServiceImpl;
import com.axelor.apps.businessproject.service.InvoicingProjectService;
import com.axelor.apps.businessproject.service.TimesheetProjectServiceImpl;
import com.axelor.apps.production.service.CostSheetServiceImpl;
import com.axelor.apps.production.service.ManufOrderServiceImpl;
import com.axelor.apps.production.service.OperationOrderServiceImpl;
import com.axelor.apps.production.service.OperationOrderWorkflowService;
import com.axelor.apps.production.service.ProductionOrderSaleOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderWizardServiceImpl;

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
    bind(OperationOrderWorkflowService.class).to(OperationOrderWorkflowServiceBusinessImpl.class);
    bind(ManufOrderValidateBusinessService.class).to(ManufOrderValidateBusinessServiceImpl.class);
    bind(OperationOrderValidateBusinessService.class)
        .to(OperationOrderValidateBusinessServiceImpl.class);
    bind(TimesheetProjectServiceImpl.class).to(TimesheetBusinessProductionServiceImpl.class);
    bind(OperationOrderTimesheetService.class).to(OperationOrderTimesheetServiceImpl.class);
  }
}
