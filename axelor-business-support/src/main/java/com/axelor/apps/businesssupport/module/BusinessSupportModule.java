package com.axelor.apps.businesssupport.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproject.service.TeamTaskBusinessProjectServiceImpl;
import com.axelor.apps.businesssupport.service.TeamTaskBusinessSupportServiceImpl;

public class BusinessSupportModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(TeamTaskBusinessProjectServiceImpl.class).to(TeamTaskBusinessSupportServiceImpl.class);
  }
}
