package com.axelor.apps.admin.service;

import com.axelor.apps.admin.db.GlobalTrackingLog;
import com.axelor.meta.db.MetaModel;
import java.util.List;

public interface GlobalTrackingLogService {

  public GlobalTrackingLog createExportLog(MetaModel model);

  public void deleteOldGlobalTrackingLog(int months) throws Exception;

  public void removeGlobalTrackingLogs(List<GlobalTrackingLog> globalTrackingLogList);
}
