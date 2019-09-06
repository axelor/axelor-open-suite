package com.axelor.apps.portal.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.portal.service.ClientViewService;
import com.axelor.apps.portal.service.ClientViewServiceImpl;

public class ClientPortalModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ClientViewService.class).to(ClientViewServiceImpl.class);
  }
}
