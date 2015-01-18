package com.axelor.apps.production.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.production.service.BillOfMaterialServiceBusinessImpl;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.apps.production.service.ManufOrderServiceBusinessImpl;
import com.axelor.apps.production.service.ManufOrderServiceImpl;
import com.axelor.apps.production.service.OperationOrderServiceBusinessImpl;
import com.axelor.apps.production.service.OperationOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderSaleOrderServiceBusinessImpl;
import com.axelor.apps.production.service.ProductionOrderSaleOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderServiceBusinessImpl;
import com.axelor.apps.production.service.ProductionOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderWizardServiceBusinessImpl;
import com.axelor.apps.production.service.ProductionOrderWizardServiceImpl;
@AxelorModuleInfo(name="axelor-business-production")
public class BusinessProductionModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(ProductionOrderServiceImpl.class).to(ProductionOrderServiceBusinessImpl.class);
		bind(BillOfMaterialServiceImpl.class).to(BillOfMaterialServiceBusinessImpl.class);
		bind(ManufOrderServiceImpl.class).to(ManufOrderServiceBusinessImpl.class);
		bind(OperationOrderServiceImpl.class).to(OperationOrderServiceBusinessImpl.class);
		bind(ProductionOrderServiceImpl.class).to(ProductionOrderServiceBusinessImpl.class);
		bind(ProductionOrderWizardServiceImpl.class).to(ProductionOrderWizardServiceBusinessImpl.class);
		bind(ProductionOrderSaleOrderServiceImpl.class).to(ProductionOrderSaleOrderServiceBusinessImpl.class);
	}

}
 