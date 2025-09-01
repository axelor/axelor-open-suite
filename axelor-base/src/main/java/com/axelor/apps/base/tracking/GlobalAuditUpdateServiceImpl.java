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

public class GlobalAuditUpdateServiceImpl implements GlobalAuditUpdateService {

  protected final GlobalAuditService globalAuditService;

  @Inject
  public GlobalAuditUpdateServiceImpl(GlobalAuditService globalAuditService) {
    this.globalAuditService = globalAuditService;
  }

  @Override
  public void createUpdateLog(
      List<GlobalTrackingConfigurationLine> globalTrackingConfigurationLineList,
      Model entity,
      Map<String, Object> values,
      Map<String, Object> oldValues) {

    boolean skipLog = true;

    for (GlobalTrackingConfigurationLine globalTrackingConfigurationLine :
        globalTrackingConfigurationLineList) {
      Object currentValue = values.get(globalTrackingConfigurationLine.getMetaField().getName());
      Object oldValue = oldValues.get(globalTrackingConfigurationLine.getMetaField().getName());
      if (!currentValue.equals(oldValue)) {
        skipLog = false;
        break;
      }
    }

    if (skipLog) {
      return;
    }

    GlobalTrackingLog log =
        globalAuditService.addLog(entity, GlobalTrackingLogRepository.TYPE_UPDATE);
    for (GlobalTrackingConfigurationLine globalTrackingConfigurationLine :
        globalTrackingConfigurationLineList) {
      MetaField metaField = globalTrackingConfigurationLine.getMetaField();
      GlobalTrackingLogLine logLine = createUpdateLogLine(metaField, values, oldValues);
      log.addGlobalTrackingLogLineListItem(logLine);
      globalAuditService.completeLog(log, AuthUtils.getUser(), globalTrackingConfigurationLine);
    }
  }

  protected GlobalTrackingLogLine createUpdateLogLine(
      MetaField metaField, Map<String, Object> values, Map<String, Object> oldValues) {
    GlobalTrackingLogLine logLine = new GlobalTrackingLogLine();
    logLine.setMetaFieldName(metaField.getName());
    Object currentValue = values.get(metaField.getName());
    Object oldValue = oldValues.get(metaField.getName());
    if (currentValue instanceof AuditableModel || oldValue instanceof AuditableModel) {

      logLine.setNewValue(
          currentValue instanceof AuditableModel
              ? String.valueOf(((AuditableModel) currentValue).getId())
              : "");
      logLine.setPreviousValue(
          oldValue instanceof AuditableModel
              ? String.valueOf(((AuditableModel) oldValue).getId())
              : "");

    } else if (currentValue instanceof Collection || oldValue instanceof Collection) {

      String prevVal = "";
      String newVal = "";
      if (CollectionUtils.isNotEmpty((Collection<Object>) oldValue)) {
        prevVal =
            String.format(
                "[%s]",
                ((Collection<AuditableModel>) oldValue)
                    .stream()
                        .map(AuditableModel::getId)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
      }
      if (CollectionUtils.isNotEmpty((Collection<Object>) currentValue)) {
        newVal =
            String.format(
                "[%s]",
                ((Collection<AuditableModel>) currentValue)
                    .stream()
                        .map(AuditableModel::getId)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
      }
      logLine.setPreviousValue(prevVal);
      logLine.setNewValue(newVal);

    } else {
      logLine.setNewValue(String.valueOf(Optional.ofNullable(currentValue).orElse("")));
      logLine.setPreviousValue(String.valueOf(Optional.ofNullable(oldValue).orElse("")));
    }
    return logLine;
  }
}
