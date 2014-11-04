package com.axelor.apps.production.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.production.db.repo.ManufOrderManagementRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
@AxelorModuleInfo(name="axelor-production")
public class ProductionModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(ManufOrderRepository.class).to(ManufOrderManagementRepository.class);
	}

}
