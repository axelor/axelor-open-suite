package com.axelor.admin.auth;

import com.axelor.apps.admin.db.GlobalTrackingLog;
import com.axelor.apps.admin.db.GlobalTrackingLogLine;
import com.axelor.apps.admin.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuditInterceptor;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Transaction;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentSet;

public class GlobalAuditTracker {

  private static final ThreadLocal<List<GlobalTrackingLog>> LOGS = new ThreadLocal<>();

  /**
   * This method should be called from {@link
   * AuditInterceptor#beforeTransactionCompletion(Transaction)} method to finish change recording.
   *
   * @param tx the transaction in which the change tracking is being done
   * @param user the session user
   */
  public void onComplete(Transaction tx, User user) {

    List<GlobalTrackingLog> logList = LOGS.get();
    LOGS.remove();
    if (CollectionUtils.isEmpty(logList)) {
      return;
    }

    MetaModelRepository modelRepo = Beans.get(MetaModelRepository.class);
    MetaFieldRepository fieldRepo = Beans.get(MetaFieldRepository.class);
    GlobalTrackingLogRepository logRepo = Beans.get(GlobalTrackingLogRepository.class);

    for (GlobalTrackingLog log : logList) {
      log.setMetaModel(modelRepo.findByName(log.getMetaModelName()));
      if ((CollectionUtils.isNotEmpty(log.getGlobalTrackingLogLineList())
              || GlobalTrackingLogRepository.TYPE_DELETE == log.getTypeSelect())
          && log.getMetaModel() != null) {
        for (GlobalTrackingLogLine line : log.getGlobalTrackingLogLineList()) {
          line.setMetaField(
              fieldRepo
                  .all()
                  .filter(
                      "self.metaModel.id = ? AND self.name = ?",
                      log.getMetaModel().getId(),
                      line.getMetaFieldName())
                  .fetchOne());
        }
        log.setUser(user);
        logRepo.save(log);
      }
    }
  }

  protected void init() {
    LOGS.set(new ArrayList<>());
  }

  /**
   * This method should be called from {@link
   * AuditInterceptor#afterTransactionCompletion(Transaction)} method to clear the change recording.
   */
  protected void clear() {
    LOGS.remove();
  }

  protected void addLog(GlobalTrackingLog log) {
    LOGS.get().add(log);
  }

  protected GlobalTrackingLog addLog(AuditableModel entity, int type) {

    GlobalTrackingLog log = new GlobalTrackingLog();
    log.setDateT(LocalDateTime.now());
    log.setMetaModelName(entity.getClass().getSimpleName());
    log.setTypeSelect(type);
    log.setUser(AuthUtils.getUser());
    log.setRelatedId((Long) entity.getId());
    log.setGlobalTrackingLogLineList(new ArrayList<>());
    LOGS.get().add(log);
    return log;
  }

  @SuppressWarnings("unchecked")
  protected void addCollectionModification(Object collection, Long id) {

    if (collection instanceof AbstractPersistentCollection) {

      AbstractPersistentCollection newValues = null;
      Collection<AuditableModel> oldValues = null;
      if (collection instanceof PersistentSet) {
        // MANY-TO-MANY
        newValues = (PersistentSet) collection;
        oldValues =
            (Collection<AuditableModel>) ((Map<?, ?>) newValues.getStoredSnapshot()).keySet();
      } else if (collection instanceof PersistentBag) {
        // ONE-TO-MANY
        newValues = (PersistentBag) collection;
        oldValues = (Collection<AuditableModel>) newValues.getStoredSnapshot();
      }

      Object owner = newValues.getOwner();

      String fieldName = newValues.getRole().replace(owner.getClass().getCanonicalName() + ".", "");

      GlobalTrackingLog log =
          LOGS.get()
              .stream()
              .filter(
                  l ->
                      l.getRelatedId().equals(id)
                          && l.getMetaModelName().equals(owner.getClass().getSimpleName()))
              .findFirst()
              .orElse(addLog((AuditableModel) owner, GlobalTrackingLogRepository.TYPE_UPDATE));

      List<Long> previousIdList = new ArrayList<>();
      List<Long> newIdList = new ArrayList<>();

      if (CollectionUtils.isNotEmpty(oldValues)) {
        for (AuditableModel oldValue : oldValues) {
          if (oldValue != null) {
            previousIdList.add(oldValue.getId());
          }
        }
      }

      for (AuditableModel newValue : (Collection<AuditableModel>) newValues) {
        if (newValue != null) {
          newIdList.add(newValue.getId());
        }
      }

      GlobalTrackingLogLine line =
          log.getGlobalTrackingLogLineList()
              .stream()
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
  }
}
