package com.axelor.apps.gdpr.service.app;

import com.axelor.apps.base.db.AppGdpr;
import com.axelor.db.Query;

public class AppGdprServiceImpl implements AppGdprService {
  @Override
  public AppGdpr getAppGDPR() {
    return Query.of(AppGdpr.class).cacheable().fetchOne();
  }
}
