package com.axelor.apps.production.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.production.db.repo.ManufOrderManagementRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.apps.production.service.ManufOrderService;
import com.axelor.apps.production.service.ManufOrderServiceImpl;
import com.axelor.apps.production.service.OperationOrderService;
import com.axelor.apps.production.service.OperationOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderSaleOrderService;
import com.axelor.apps.production.service.ProductionOrderSaleOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderService;
import com.axelor.apps.production.service.ProductionOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderWizardService;
import com.axelor.apps.production.service.ProductionOrderWizardServiceImpl;
@AxelorModuleInfo(name="axelor-production")
public class ProductionModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(ManufOrderRepository.class).to(ManufOrderManagementRepository.class);
		bind(ProductionOrderService.class).to(ProductionOrderServiceImpl.class);
		bind(BillOfMaterialService.class).to(BillOfMaterialServiceImpl.class);
		bind(ManufOrderService.class).to(ManufOrderServiceImpl.class);
		bind(OperationOrderService.class).to(OperationOrderServiceImpl.class);
		bind(ProductionOrderService.class).to(ProductionOrderServiceImpl.class);
		bind(ProductionOrderWizardService.class).to(ProductionOrderWizardServiceImpl.class);		
		bind(ProductionOrderSaleOrderService.class).to(ProductionOrderSaleOrderServiceImpl.class);


	}

}
