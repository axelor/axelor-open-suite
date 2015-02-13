package com.axelor.apps.human.resource.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.base.service.user.UserServiceImpl;
import com.axelor.apps.human.resource.service.user.UserServiceHr;
import com.axelor.apps.human.resource.service.user.UserServiceHrImpl;

@AxelorModuleInfo(name = "axelor-base")
public class HumanResourceModule extends AxelorModule {

	@Override
	protected void configure() {
		
		bind(UserServiceHr.class).to(UserServiceHrImpl.class);
		bind(UserServiceImpl.class).to(UserServiceHrImpl.class);
	}

}
