package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import java.util.List;

public interface GlobalTrackingConfigurationLineFetchService {
  List<GlobalTrackingConfigurationLine> getGlobalTrackingConfigurationLines(
      List<String> entitiesName);
}
