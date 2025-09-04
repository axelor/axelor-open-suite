package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.db.Model;
import java.util.List;
import java.util.Map;

public interface GlobalAuditUpdateService {
  void createUpdateLog(
      List<GlobalTrackingConfigurationLine> globalTrackingConfigurationLineList,
      Model entity,
      Map<String, Object> values,
      Map<String, Object> oldValues);
}
