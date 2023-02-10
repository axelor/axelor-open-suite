package com.axelor.apps.gdpr.service;

import com.axelor.apps.gdpr.db.GDPRErasureLog;
import com.axelor.apps.gdpr.db.GDPRResponse;
import com.axelor.meta.db.MetaModel;

public class GdprErasureLogServiceImpl implements GdprErasureLogService {

  @Override
  public GDPRErasureLog createErasureLogLine(
      GDPRResponse gdprResponse, MetaModel metaModel, Integer numberRecords) {

    GDPRErasureLog gdprErasureLog = new GDPRErasureLog();
    gdprErasureLog.setModelLog(metaModel);
    gdprErasureLog.setNumberOfrecords(numberRecords);
    gdprErasureLog.setGdprResponse(gdprResponse);

    return gdprErasureLog;
  }
}
