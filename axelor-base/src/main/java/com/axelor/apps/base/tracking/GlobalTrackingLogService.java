package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import java.util.List;

public interface GlobalTrackingLogService {

  public GlobalTrackingLog createExportLog(MetaModel model, MetaFile metaFile);

  public void deleteOldGlobalTrackingLog(int months);

  public void removeGlobalTrackingLogs(List<GlobalTrackingLog> globalTrackingLogList);
}
