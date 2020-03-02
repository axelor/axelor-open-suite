package com.axelor.apps.admin.service;

import com.axelor.apps.admin.db.GlobalTrackingLog;
import com.axelor.meta.db.MetaModel;

public interface GlobalTrackingLogService {

  public GlobalTrackingLog createExportLog(MetaModel model);
}
