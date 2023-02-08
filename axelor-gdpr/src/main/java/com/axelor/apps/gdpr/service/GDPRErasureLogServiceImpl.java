package com.axelor.apps.gdpr.service;

import com.axelor.apps.gdpr.db.GDPRErasureLog;
import com.axelor.apps.gdpr.db.GDPRErasureResponse;
import com.axelor.meta.db.MetaModel;

public class GDPRErasureLogServiceImpl implements GDPRErasureLogService {

  @Override
  public GDPRErasureLog createErasureLogLine(
      GDPRErasureResponse gdprErasureResponse, MetaModel metaModel, Integer numberRecords) {

    GDPRErasureLog gdprErasureLog = new GDPRErasureLog();
    gdprErasureLog.setModelLog(metaModel);
    gdprErasureLog.setNumberOfrecords(numberRecords);
    gdprErasureLog.setGdprErasureResponse(gdprErasureResponse);

    return gdprErasureLog;
  }
}
