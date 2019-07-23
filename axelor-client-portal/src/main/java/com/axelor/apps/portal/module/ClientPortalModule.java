package com.axelor.apps.portal.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.portal.service.ClientViewInterface;
import com.axelor.apps.portal.service.ClientViewInterfaceImpl;

public class ClientPortalModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ClientViewInterface.class).to(ClientViewInterfaceImpl.class);
  }
}
