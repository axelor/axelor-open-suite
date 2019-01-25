package com.axelor.apps.businesssupport.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.businessproject.service.TeamTaskBusinessServiceImpl;
import com.axelor.apps.businesssupport.service.TeamTaskBusinessSupportServiceImpl;

public class BusinessSupportModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(TeamTaskBusinessServiceImpl.class).to(TeamTaskBusinessSupportServiceImpl.class);
  }
}
