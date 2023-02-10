package com.axelor.apps.gdpr.service;

import com.axelor.apps.gdpr.db.GDPRErasureLog;
import com.axelor.apps.gdpr.db.GDPRResponse;
import com.axelor.meta.db.MetaModel;

public interface GdprErasureLogService {

  GDPRErasureLog createErasureLogLine(
      GDPRResponse gdprResponse, MetaModel metaModel, Integer numberRecords);
}
