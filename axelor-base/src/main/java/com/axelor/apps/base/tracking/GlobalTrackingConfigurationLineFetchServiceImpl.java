package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.repo.GlobalTrackingConfigurationLineRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalTrackingConfigurationLineFetchServiceImpl
    implements GlobalTrackingConfigurationLineFetchService {

  protected final GlobalTrackingConfigurationLineRepository
      globalTrackingConfigurationLineRepository;

  @Inject
  public GlobalTrackingConfigurationLineFetchServiceImpl(
      GlobalTrackingConfigurationLineRepository globalTrackingConfigurationLineRepository) {
    this.globalTrackingConfigurationLineRepository = globalTrackingConfigurationLineRepository;
  }

  @Override
  public List<GlobalTrackingConfigurationLine> getGlobalTrackingConfigurationLines(
      List<String> entitiesName) {
    List<GlobalTrackingConfigurationLine> globalTrackingConfigurationLineList = new ArrayList<>();
    for (String entityName : entitiesName) {
      globalTrackingConfigurationLineList.addAll(
          globalTrackingConfigurationLineRepository
              .all()
              .filter(
                  "self.metaModel.name = ?",
                  Arrays.stream(entityName.split("\\.")).toList().getLast())
              .fetch());
    }
    return globalTrackingConfigurationLineList;
  }
}
