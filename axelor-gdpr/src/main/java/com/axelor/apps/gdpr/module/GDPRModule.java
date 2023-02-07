package com.axelor.apps.gdpr.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.gdpr.service.*;
import com.axelor.apps.gdpr.service.app.AppGDPRService;
import com.axelor.apps.gdpr.service.app.AppGDPRServiceImpl;

public class GDPRModule extends AxelorModule {
  @Override
  protected void configure() {
    bind(GDPRAccessResponseService.class).to(GDPRAccessResponseServiceImpl.class);
    bind(GDPRErasureResponseService.class).to(GDPRErasureResponseServiceImpl.class);
    bind(GDPRErasureLogService.class).to(GDPRErasureLogServiceImpl.class);
    bind(GDPRResponseService.class).to(GDPRResponseServiceImpl.class);
    bind(AppGDPRService.class).to(AppGDPRServiceImpl.class);
    bind(GDPRAnonymizeService.class).to(GDPRAnonymizeServiceImpl.class);
    bind(GDPRSearchEngineService.class).to(GDPRSearchEngineServiceImpl.class);
  }
}
