package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaField;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class GlobalAuditCreateServiceImpl implements GlobalAuditCreateService {

  protected final GlobalAuditService globalAuditService;

  @Inject
  public GlobalAuditCreateServiceImpl(GlobalAuditService globalAuditService) {
    this.globalAuditService = globalAuditService;
  }

  @Override
  public void createCreationLog(
      List<GlobalTrackingConfigurationLine> globalTrackingConfigurationLineList,
      Model entity,
      Map<String, Object> values) {
    GlobalTrackingLog log =
        globalAuditService.addLog(entity, GlobalTrackingLogRepository.TYPE_CREATE);
    for (GlobalTrackingConfigurationLine globalTrackingConfigurationLine :
        globalTrackingConfigurationLineList) {
      MetaField metaField = globalTrackingConfigurationLine.getMetaField();
      GlobalTrackingLogLine logLine = createCreationLogLine(metaField, values);
      log.addGlobalTrackingLogLineListItem(logLine);
      globalAuditService.completeLog(log, AuthUtils.getUser(), globalTrackingConfigurationLine);
    }
  }

  protected GlobalTrackingLogLine createCreationLogLine(
      MetaField metaField, Map<String, Object> values) {
    GlobalTrackingLogLine logLine = new GlobalTrackingLogLine();
    logLine.setMetaFieldName(metaField.getName());
    Object object = values.get(metaField.getName());
    if (object instanceof AuditableModel) {
      logLine.setNewValue(String.valueOf(((AuditableModel) object).getId()));
    } else if (object instanceof Collection) {
      String newVal = "";
      if (CollectionUtils.isNotEmpty((Collection<Object>) object)) {
        newVal =
            String.format(
                "[%s]",
                ((Collection<AuditableModel>) object)
                    .stream()
                        .map(AuditableModel::getId)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
      }
      logLine.setNewValue(newVal);
    } else {
      logLine.setNewValue(String.valueOf(Optional.ofNullable(object).orElse("")));
    }
    return logLine;
  }
}
