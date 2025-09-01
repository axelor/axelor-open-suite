package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.db.Model;
import java.util.List;
import java.util.Map;

public interface GlobalAuditCreateService {
  void createCreationLog(
      List<GlobalTrackingConfigurationLine> globalTrackingConfigurationLineList,
      Model entity,
      Map<String, Object> values);
}
