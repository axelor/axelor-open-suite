package com.axelor.apps.gdpr.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.gdpr.service.*;
import com.axelor.apps.gdpr.service.app.AppGdprService;
import com.axelor.apps.gdpr.service.app.AppGdprServiceImpl;
import com.axelor.apps.gdpr.service.response.GdprResponseAccessService;
import com.axelor.apps.gdpr.service.response.GdprResponseAccessServiceImpl;
import com.axelor.apps.gdpr.service.response.GdprResponseErasureService;
import com.axelor.apps.gdpr.service.response.GdprResponseErasureServiceImpl;
import com.axelor.apps.gdpr.service.response.GdprResponseService;
import com.axelor.apps.gdpr.service.response.GdprResponseServiceImpl;

public class GdprModule extends AxelorModule {
  @Override
  protected void configure() {
    bind(GdprResponseAccessService.class).to(GdprResponseAccessServiceImpl.class);
    bind(GdprResponseErasureService.class).to(GdprResponseErasureServiceImpl.class);
    bind(GdprErasureLogService.class).to(GdprErasureLogServiceImpl.class);
    bind(GdprResponseService.class).to(GdprResponseServiceImpl.class);
    bind(AppGdprService.class).to(AppGdprServiceImpl.class);
    bind(GdprAnonymizeService.class).to(GdprAnonymizeServiceImpl.class);
    bind(GdprSearchEngineService.class).to(GdprSearchEngineServiceImpl.class);
  }
}
