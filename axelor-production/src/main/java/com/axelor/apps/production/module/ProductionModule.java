/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.apps.production.service.CostSheetLineService;
import com.axelor.apps.production.service.CostSheetLineServiceImpl;
import com.axelor.apps.production.service.CostSheetService;
import com.axelor.apps.production.service.CostSheetServiceImpl;
import com.axelor.apps.production.service.ManufOrderService;
import com.axelor.apps.production.service.ManufOrderServiceImpl;
import com.axelor.apps.production.service.MrpLineServiceProductionImpl;
import com.axelor.apps.production.service.MrpServiceProductionImpl;
import com.axelor.apps.production.service.OperationOrderService;
import com.axelor.apps.production.service.OperationOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderSaleOrderService;
import com.axelor.apps.production.service.ProductionOrderSaleOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderService;
import com.axelor.apps.production.service.ProductionOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderWizardService;
import com.axelor.apps.production.service.ProductionOrderWizardServiceImpl;
import com.axelor.apps.production.service.SaleOrderServiceProductionImpl;
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
		bind(BillOfMaterialRepository.class).to(BillOfMaterialManagementRepository.class);
	}

}
