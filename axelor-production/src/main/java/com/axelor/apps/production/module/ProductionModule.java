/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.production.db.repo.BillOfMaterialManagementRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ManufOrderManagementRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderManagementRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.*;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.app.AppProductionServiceImpl;
import com.axelor.apps.supplychain.service.StockRulesServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.MrpLineServiceImpl;
import com.axelor.apps.supplychain.service.MrpServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderServiceSupplychainImpl;

public class ProductionModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(ManufOrderRepository.class).to(ManufOrderManagementRepository.class);
		bind(OperationOrderRepository.class).to(OperationOrderManagementRepository.class);
		bind(ProductionOrderService.class).to(ProductionOrderServiceImpl.class);
		bind(BillOfMaterialService.class).to(BillOfMaterialServiceImpl.class);
		bind(ManufOrderService.class).to(ManufOrderServiceImpl.class);
		bind(OperationOrderService.class).to(OperationOrderServiceImpl.class);
		bind(ProductionOrderService.class).to(ProductionOrderServiceImpl.class);
		bind(ProductionOrderWizardService.class).to(ProductionOrderWizardServiceImpl.class);		
		bind(ProductionOrderSaleOrderService.class).to(ProductionOrderSaleOrderServiceImpl.class);
		bind(MrpLineServiceImpl.class).to(MrpLineServiceProductionImpl.class);
		bind(MrpServiceImpl.class).to(MrpServiceProductionImpl.class);
		bind(CostSheetService.class).to(CostSheetServiceImpl.class);
		bind(CostSheetLineService.class).to(CostSheetLineServiceImpl.class);
		bind(SaleOrderServiceSupplychainImpl.class).to(SaleOrderServiceProductionImpl.class);
		bind(StockRulesServiceSupplychainImpl.class).to(StockRulesServiceProductionImpl.class);
		bind(BillOfMaterialRepository.class).to(BillOfMaterialManagementRepository.class);
		bind(AppProductionService.class).to(AppProductionServiceImpl.class);
		bind(ConfiguratorCreatorService.class).to(ConfiguratorCreatorServiceImpl.class);
	}

}
