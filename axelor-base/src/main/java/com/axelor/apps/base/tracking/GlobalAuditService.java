package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.auth.db.User;
import com.axelor.db.Model;

public interface GlobalAuditService {
  GlobalTrackingLog completeLog(
      GlobalTrackingLog log, User user, GlobalTrackingConfigurationLine configLine);

  GlobalTrackingLog addLog(Model entity, int type);
}
