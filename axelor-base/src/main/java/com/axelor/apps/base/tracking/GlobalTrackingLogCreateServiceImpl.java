package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.db.Model;
import com.axelor.db.audit.AuditTracker;
import com.axelor.events.internal.BeforeTransactionComplete;
import com.axelor.meta.db.MetaModel;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class GlobalTrackingLogCreateServiceImpl implements GlobalTrackingLogCreateService {

  protected final GlobalTrackingConfigurationLineFetchService
      globalTrackingConfigurationLineFetchService;
  protected final GlobalAuditCreateService globalAuditCreateService;
  protected final GlobalAuditUpdateService globalAuditUpdateService;
  protected final GlobalAuditService globalAuditService;

  @Inject
  public GlobalTrackingLogCreateServiceImpl(
      GlobalTrackingConfigurationLineFetchService globalTrackingConfigurationLineFetchService,
      GlobalAuditCreateService globalAuditCreateService,
      GlobalAuditUpdateService globalAuditUpdateService,
      GlobalAuditService globalAuditService) {
    this.globalTrackingConfigurationLineFetchService = globalTrackingConfigurationLineFetchService;
    this.globalAuditCreateService = globalAuditCreateService;
    this.globalAuditUpdateService = globalAuditUpdateService;
    this.globalAuditService = globalAuditService;
  }

  @Override
  public void createLogForStoredEntities(BeforeTransactionComplete event) {
    Map<String, Map<Long, AuditTracker.EntityState>> stored = event.getStore();
    List<String> entitiesName = stored.keySet().stream().distinct().toList();
    List<GlobalTrackingConfigurationLine> globalTrackingConfigurationLineList =
        globalTrackingConfigurationLineFetchService.getGlobalTrackingConfigurationLines(
            entitiesName);

    if (!CollectionUtils.isEmpty(globalTrackingConfigurationLineList)) {
      List<String> entityNameToTrack =
          globalTrackingConfigurationLineList.stream()
              .map(GlobalTrackingConfigurationLine::getMetaModel)
              .map(MetaModel::getFullName)
              .distinct()
              .toList();
      for (String entityName : entityNameToTrack) {
        Map<Long, AuditTracker.EntityState> entityStateMap = stored.get(entityName);
        for (Long entityId : entityStateMap.keySet()) {
          createLog(entityId, entityStateMap, globalTrackingConfigurationLineList);
        }
      }
    }
  }

  @Override
  public void createLogForDeletedEntities(BeforeTransactionComplete event) {
    if (!event.getDeleted().isEmpty()) {
      List<String> configLineForDelete =
          globalTrackingConfigurationLineFetchService
              .getGlobalTrackingConfigurationLines(
                  event.getDeleted().stream()
                      .map(Object::getClass)
                      .map(Class::getName)
                      .collect(Collectors.toList()))
              .stream()
              .map(GlobalTrackingConfigurationLine::getMetaModel)
              .map(MetaModel::getFullName)
              .distinct()
              .toList();

      if (!configLineForDelete.isEmpty()) {
        for (String globalTrackingConfigurationLine : configLineForDelete) {
          Set<Model> modelsDeletedToTrack =
              event.getDeleted().stream()
                  .filter(
                      model -> model.getClass().getName().equals(globalTrackingConfigurationLine))
                  .collect(Collectors.toSet());
          for (Model m : modelsDeletedToTrack) {
            GlobalTrackingLog log =
                globalAuditService.addLog(m, GlobalTrackingLogRepository.TYPE_DELETE);
            globalAuditService.completeLog(log, AuthUtils.getUser(), null);
          }
        }
      }
    }
  }

  protected void createLog(
      Long entityId,
      Map<Long, AuditTracker.EntityState> entityStateMap,
      List<GlobalTrackingConfigurationLine> globalTrackingConfigurationLineList) {
    AuditTracker.EntityState entityState = entityStateMap.get(entityId);
    Map<String, Object> oldValues = entityState.getOldValues();
    Map<String, Object> values = entityState.getValues();
    Model entity = entityState.getEntity();

    if (oldValues.isEmpty()) {
      globalAuditCreateService.createCreationLog(
          globalTrackingConfigurationLineList, entity, values);

    } else {
      globalAuditUpdateService.createUpdateLog(
          globalTrackingConfigurationLineList, entity, values, oldValues);
    }
  }
}
