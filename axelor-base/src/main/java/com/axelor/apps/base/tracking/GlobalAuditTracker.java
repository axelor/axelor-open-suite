/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.db.repo.GlobalTrackingConfigurationLineRepository;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuditInterceptor;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.google.common.base.Strings;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
    GlobalTrackingConfigurationLineRepository configLineRepo =
        Beans.get(GlobalTrackingConfigurationLineRepository.class);
    GlobalTrackingConfigurationLine configLine;
    List<GlobalTrackingConfigurationLine> configLineList;
    ScriptBindings bindings;

    for (GlobalTrackingLog log : logList) {

      configLineList =
          configLineRepo.all().filter("self.metaModel.name = ?", log.getMetaModelName()).fetch();

      if (configLineList.isEmpty()) {
        continue;
      }

      log.setMetaModel(modelRepo.findByName(log.getMetaModelName()));

      List<GlobalTrackingLogLine> logLinesToSave = new ArrayList<>();

      if ((CollectionUtils.isNotEmpty(log.getGlobalTrackingLogLineList()))) {
        try {
          bindings =
              new ScriptBindings(
                  this.getContext(
                      JPA.find(
                          (Class<Model>) Class.forName(log.getMetaModel().getFullName()),
                          log.getRelatedId())));
        } catch (Exception e) {
          continue;
        }
        for (GlobalTrackingLogLine line : log.getGlobalTrackingLogLineList()) {

          configLine =
              configLineList.stream()
                  .filter(l -> l.getMetaField().getName().equals(line.getMetaFieldName()))
                  .findFirst()
                  .orElse(null);

          if (configLine == null
              || !this.canTrack(configLine, log.getTypeSelect())
              || (!Strings.isNullOrEmpty(configLine.getTrackingCondition())
                  && !Boolean.TRUE.equals(
                      new GroovyScriptHelper(bindings).eval(configLine.getTrackingCondition())))) {
            continue;
          }

          line.setMetaField(
              fieldRepo
                  .all()
                  .filter(
                      "self.metaModel.id = ? AND self.name = ?",
                      log.getMetaModel().getId(),
                      line.getMetaFieldName())
                  .fetchOne());
          logLinesToSave.add(line);
        }
      }
      if (!logLinesToSave.isEmpty()
          || (GlobalTrackingLogRepository.TYPE_DELETE == log.getTypeSelect()
              && configLineList.stream()
                  .anyMatch(l -> Boolean.TRUE.equals(l.getTrackDeletion())))) {
        log.getGlobalTrackingLogLineList().stream().forEach(l -> l.setGlobalTrackingLog(null));
        logLinesToSave.stream().forEach(l -> l.setGlobalTrackingLog(log));
        log.setUser(user);
        logRepo.save(log);
      }
    }
  }

  protected boolean canTrack(GlobalTrackingConfigurationLine confLine, int typeSelect) {

    switch (typeSelect) {
      case GlobalTrackingLogRepository.TYPE_CREATE:
        return confLine.getTrackCreation();
      case GlobalTrackingLogRepository.TYPE_READ:
        return confLine.getTrackReading();
      case GlobalTrackingLogRepository.TYPE_UPDATE:
        return confLine.getTrackUpdate();
      case GlobalTrackingLogRepository.TYPE_DELETE:
        return confLine.getTrackDeletion();
      case GlobalTrackingLogRepository.TYPE_EXPORT:
        return confLine.getTrackExport();
      default:
        return false;
    }
  }

  private Map<String, Object> getContext(Object obj)
      throws IntrospectionException, InvocationTargetException, IllegalAccessException,
          IllegalArgumentException {
    Map<String, Object> result = new HashMap<>();
    BeanInfo info = Introspector.getBeanInfo(obj.getClass());
    Method reader = null;
    for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
      reader = pd.getReadMethod();
      if (reader != null) {
        result.put(pd.getName(), reader.invoke(obj));
      }
    }
    return result;
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
    if (LOGS.get() == null) {
      this.init();
    }
    LOGS.get().add(log);
  }

  protected GlobalTrackingLog addLog(AuditableModel entity, int type) {

    GlobalTrackingLog log = new GlobalTrackingLog();
    log.setDateT(LocalDateTime.now());
    log.setMetaModelName(entity.getClass().getSimpleName());
    log.setTypeSelect(type);
    log.setUser(AuthUtils.getUser());
    log.setRelatedId(entity.getId());
    log.setRelatedReference(this.getRelatedReference(entity));
    log.setGlobalTrackingLogLineList(new ArrayList<>());

    this.addLog(log);
    return log;
  }

  protected String getRelatedReference(AuditableModel entity) {
    Mapper classMapper = Mapper.of(EntityHelper.getEntityClass(entity));

    if (classMapper.getNameField() != null && classMapper.getNameField().getName() != null) {
      String fieldName = classMapper.getNameField().getName();
      return (String) Mapper.toMap(entity).get(fieldName);
    }

    return "";
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

      if (newValues == null) {
        return;
      }

      Object owner = newValues.getOwner();

      if (owner == null
          || Arrays.asList(GlobalAuditInterceptor.BACKLISTED_CLASSES).contains(owner.getClass())
          || !(owner instanceof AuditableModel)) {
        return;
      }

      String fieldName = newValues.getRole().replace(owner.getClass().getCanonicalName() + ".", "");

      GlobalTrackingLog log =
          LOGS.get().stream()
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
  }
}
