package com.axelor.apps.contract.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.contract.service.ContractService;
import com.axelor.apps.contract.service.ContractServiceImpl;
import com.axelor.apps.contract.service.ContractVersionService;
import com.axelor.apps.contract.service.ContractVersionServiceImpl;

public class ContractModule extends AxelorModule {

	@Override
	protected void configure() {
		bind(ContractService.class).to(ContractServiceImpl.class);
		bind(ContractVersionService.class).to(ContractVersionServiceImpl.class);
	}

}
