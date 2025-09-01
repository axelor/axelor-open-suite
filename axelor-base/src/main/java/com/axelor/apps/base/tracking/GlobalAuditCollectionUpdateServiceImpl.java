package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.StringUtils;
import com.axelor.db.Model;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.collection.spi.AbstractPersistentCollection;
import org.hibernate.collection.spi.PersistentBag;
import org.hibernate.collection.spi.PersistentSet;

public class GlobalAuditCollectionUpdateServiceImpl implements GlobalAuditCollectionUpdateService {

  protected final GlobalAuditService globalAuditService;
  protected final GlobalTrackingConfigurationLineFetchService
      globalTrackingConfigurationLineFetchService;

  @Inject
  public GlobalAuditCollectionUpdateServiceImpl(
      GlobalAuditService globalAuditService,
      GlobalTrackingConfigurationLineFetchService globalTrackingConfigurationLineFetchService) {
    this.globalAuditService = globalAuditService;
    this.globalTrackingConfigurationLineFetchService = globalTrackingConfigurationLineFetchService;
  }

  @Override
  public void createLogForCollectionUpdate(
      List<GlobalAuditTracker.CollectionState> collectionStateList) {
    if (CollectionUtils.isEmpty(collectionStateList)) {
      return;
    }

    List<GlobalTrackingConfigurationLine> configurationLines =
        getGlobalTrackingConfigurationLines(collectionStateList);
    if (CollectionUtils.isEmpty(configurationLines)) {
      return;
    }

    for (GlobalAuditTracker.CollectionState collectionState : collectionStateList) {
      Model owner = collectionState.owner();
      Object collection = collectionState.collection();
      Object oldCollection = collectionState.oldCollection();
      AbstractPersistentCollection newValues = null;
      if (collection instanceof PersistentSet) {
        newValues = (PersistentSet) collection;
      } else if (collection instanceof PersistentBag) {
        newValues = (PersistentBag) collection;
      }
      String fieldName = getFieldName(newValues.getRole(), owner);
      for (GlobalTrackingConfigurationLine configLine : configurationLines) {
        if (configLine.getMetaModel().getFullName().equals(owner.getClass().getName())
            && configLine.getMetaField().getName().equals(fieldName)) {
          addCollectionModification(collection, oldCollection, configLine);
        }
      }
    }
  }

  protected void addCollectionModification(
      Object collection, Object oldCollection, GlobalTrackingConfigurationLine configLine) {
    if (!(collection instanceof AbstractPersistentCollection)) {
      return;
    }

    AbstractPersistentCollection newValues = null;
    Collection<AuditableModel> oldValues = null;
    if (collection instanceof PersistentSet) {
      // MANY-TO-MANY
      newValues = (PersistentSet) collection;
      oldValues = (Collection<AuditableModel>) oldCollection;
    } else if (collection instanceof PersistentBag) {
      // ONE-TO-MANY
      newValues = (PersistentBag) collection;
      oldValues = (Collection<AuditableModel>) oldCollection;
    }

    if (newValues == null) {
      return;
    }

    createLog(configLine, newValues, oldValues);
  }

  protected void createLog(
      GlobalTrackingConfigurationLine configLine,
      AbstractPersistentCollection newValues,
      Collection<AuditableModel> oldValues) {
    Model owner = (Model) newValues.getOwner();
    String fieldName = getFieldName(newValues.getRole(), owner);

    GlobalTrackingLog log =
        globalAuditService.addLog((AuditableModel) owner, GlobalTrackingLogRepository.TYPE_UPDATE);

    List<Long> previousIdList = getPreviousIdList(oldValues);
    List<Long> newIdList = getNewIdList((Collection<AuditableModel>) newValues);
    createLineLog(log, fieldName, previousIdList, newIdList);
    globalAuditService.completeLog(log, AuthUtils.getUser(), configLine);
  }

  protected void createLineLog(
      GlobalTrackingLog log, String fieldName, List<Long> previousIdList, List<Long> newIdList) {
    GlobalTrackingLogLine line =
        log.getGlobalTrackingLogLineList().stream()
            .filter(l -> l.getMetaFieldName().equals(fieldName))
            .findFirst()
            .orElse(null);

    if (line == null) {
      line = new GlobalTrackingLogLine();
      line.setMetaFieldName(fieldName);
      line.setGlobalTrackingLog(log);
      line.setPreviousValue(
          String.format(
              "[%s]",
              previousIdList.stream().map(String::valueOf).collect(Collectors.joining(", "))));
      log.addGlobalTrackingLogLineListItem(line);
    }
    line.setNewValue(
        String.format(
            "[%s]", newIdList.stream().map(String::valueOf).collect(Collectors.joining(", "))));
  }

  protected List<GlobalTrackingConfigurationLine> getGlobalTrackingConfigurationLines(
      List<GlobalAuditTracker.CollectionState> collectionStateList) {
    List<String> modelNameList =
        collectionStateList.stream()
            .map(GlobalAuditTracker.CollectionState::owner)
            .map(Model::getClass)
            .map(Class::getName)
            .toList();
    return globalTrackingConfigurationLineFetchService
        .getGlobalTrackingConfigurationLines(modelNameList)
        .stream()
        .filter(
            configLine ->
                configLine.getMetaField().getRelationship() != null
                    && (configLine.getMetaField().getRelationship().equals("OneToMany")
                        || configLine.getMetaField().getRelationship().equals("ManyToOne")))
        .toList();
  }

  protected String getFieldName(String role, Model owner) {
    if (StringUtils.isBlank(role) || owner == null) {
      return "";
    }
    return role.replace(owner.getClass().getCanonicalName() + ".", "");
  }

  protected List<Long> getNewIdList(Collection<AuditableModel> newValues) {
    List<Long> newIdList = new ArrayList<>();
    for (AuditableModel newValue : newValues) {
      if (newValue != null) {
        newIdList.add(newValue.getId());
      }
    }
    return newIdList;
  }

  protected List<Long> getPreviousIdList(Collection<AuditableModel> oldValues) {
    List<Long> previousIdList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(oldValues)) {
      for (AuditableModel oldValue : oldValues) {
        if (oldValue != null) {
          previousIdList.add(oldValue.getId());
        }
      }
    }
    return previousIdList;
  }
}
