package com.axelor.apps.gdpr.service;

import com.axelor.apps.gdpr.db.GDPRErasureLog;
import com.axelor.apps.gdpr.db.GDPRErasureResponse;
import com.axelor.meta.db.MetaModel;

public interface GDPRErasureLogService {

  public GDPRErasureLog createErasureLogLine(
      GDPRErasureResponse gdprErasureResponse, MetaModel metaModel, Integer numberRecords);
}
