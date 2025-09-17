/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import static org.eclipse.birt.data.engine.olap.data.api.cube.TimeDimensionUtil.getFieldName;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.StringUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.internal.DBHelper;
import com.axelor.db.mapper.Mapper;
import com.axelor.event.Event;
import com.axelor.events.internal.BeforeTransactionComplete;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.FlushMode;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.collection.spi.AbstractPersistentCollection;
import org.hibernate.collection.spi.PersistentBag;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.collection.spi.PersistentSet;
import org.hibernate.engine.spi.SessionImplementor;

public class GlobalAuditTracker implements BeforeTransactionCompletionProcess {

  private final Map<String, Map<Long, EntityState>> store = new HashMap<>();
  private final Set<Model> updated = new HashSet<>();
  private final Set<Model> deleted = new HashSet<>();
  private final List<CollectionState> updatedCollections = new ArrayList<>();

  private GlobalTrackingLogRepository globalTrackingLogRepository;

  public record CollectionState(
      Model owner,
      Collection<? extends Model> collection,
      Collection<? extends Model> oldCollection) {}

  public void track(Model entity, String[] names, Object[] state, Object[] previousState) {

    final Map<String, Object> values = new HashMap<>();
    final Map<String, Object> oldValues = new HashMap<>();

    for (int i = 0; i < names.length; i++) {
      values.put(names[i], state[i]);
    }

    if (previousState != null) {
      for (int i = 0; i < names.length; i++) {
        oldValues.put(names[i], previousState[i]);
      }
    }

    final Long id = entity.getId();
    final String key = EntityHelper.getEntityClass(entity).getName();
    final Map<Long, EntityState> entityStates = store.computeIfAbsent(key, key_ -> new HashMap<>());

    final EntityState entityState =
        entityStates.computeIfAbsent(
            id,
            id_ -> {
              EntityState newEntityState = new EntityState();
              newEntityState.entity = entity;
              newEntityState.values = values;
              newEntityState.oldValues = oldValues;
              return newEntityState;
            });

    if (entityState.values != values) {
      entityState.values.putAll(values);
    }
  }

  public void deleted(Model entity) {
    deleted.add(entity);
  }

  public void updated(Model entity) {
    updated.add(entity);
  }

  private void process(EntityState state) {

    final Model entity = state.entity;
    final Map<String, Object> values = state.values;
    final Map<String, Object> oldValues = state.oldValues;
    final Map<String, Object> previousState = oldValues.isEmpty() ? null : oldValues;
    List<GlobalTrackingConfigurationLine> configurationLines =
        getGlobalTrackingConfigLines(entity).stream()
            .filter(
                configLine ->
                    configLine.getMetaField().getRelationship() == null
                        || (configLine.getMetaField().getRelationship() == null
                            && (configLine.getMetaField().getRelationship().equals("ManyToOne")
                                || configLine.getMetaField().getRelationship().equals("OneToOne"))))
            .toList();
    if (CollectionUtils.isEmpty(configurationLines)) {
      return;
    }

    if (previousState != null) {
      createUpdateLog(configurationLines, entity, values, oldValues);
    } else {
      createCreationLog(configurationLines, entity, values);
    }
  }

  protected void createCreationLog(
      List<GlobalTrackingConfigurationLine> globalTrackingConfigurationLineList,
      Model entity,
      Map<String, Object> values) {
    GlobalTrackingLog log = createLog(entity, GlobalTrackingLogRepository.TYPE_CREATE);
    for (GlobalTrackingConfigurationLine globalTrackingConfigurationLine :
        globalTrackingConfigurationLineList) {
      MetaField metaField = globalTrackingConfigurationLine.getMetaField();
      if (isNotCollectionField(metaField)) {
        GlobalTrackingLogLine logLine =
            createCreationLogLine(log, globalTrackingConfigurationLine, metaField, values);
        if (logLine != null) {
          log.addGlobalTrackingLogLineListItem(logLine);
        }
      }
    }
    getGlobalTrackingLogRepository().save(log);
  }

  protected GlobalTrackingLogLine createCreationLogLine(
      GlobalTrackingLog log,
      GlobalTrackingConfigurationLine configLine,
      MetaField metaField,
      Map<String, Object> values) {
    if (!isLineToBeAdded(log, configLine)) {
      return null;
    }
    GlobalTrackingLogLine logLine = new GlobalTrackingLogLine();
    String metaFieldName = metaField.getName();
    logLine.setMetaFieldName(metaFieldName);
    logLine.setMetaField(metaField);
    Object object = values.get(metaFieldName);
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

  public void createUpdateLog(
      List<GlobalTrackingConfigurationLine> globalTrackingConfigurationLineList,
      Model entity,
      Map<String, Object> values,
      Map<String, Object> oldValues) {

    GlobalTrackingLog log = createLog(entity, GlobalTrackingLogRepository.TYPE_UPDATE);
    for (GlobalTrackingConfigurationLine globalTrackingConfigurationLine :
        globalTrackingConfigurationLineList) {
      MetaField metaField = globalTrackingConfigurationLine.getMetaField();
      if (isNotCollectionField(metaField)) {
        GlobalTrackingLogLine logLine =
            createUpdateLogLine(log, globalTrackingConfigurationLine, metaField, values, oldValues);
        if (logLine != null) {
          log.addGlobalTrackingLogLineListItem(logLine);
        }
      }
    }
    if (CollectionUtils.isEmpty(log.getGlobalTrackingLogLineList())) {
      return;
    }
    getGlobalTrackingLogRepository().save(log);
  }

  protected GlobalTrackingLogLine createUpdateLogLine(
      GlobalTrackingLog log,
      GlobalTrackingConfigurationLine configLine,
      MetaField metaField,
      Map<String, Object> values,
      Map<String, Object> oldValues) {
    if (!isLineToBeAdded(log, configLine)) {
      return null;
    }
    String metaFieldName = metaField.getName();
    Object currentValue = values.get(metaFieldName);
    Object oldValue = oldValues.get(metaFieldName);
    if (currentValue.equals(oldValue)) {
      return null;
    }
    GlobalTrackingLogLine logLine = new GlobalTrackingLogLine();
    logLine.setMetaFieldName(metaFieldName);
    logLine.setMetaField(metaField);

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

  protected GlobalTrackingLog createLog(Model entity, int type) {

    GlobalTrackingLog log = new GlobalTrackingLog();
    log.setDateT(LocalDateTime.now());
    log.setMetaModelName(entity.getClass().getSimpleName());
    log.setMetaModel(
        Query.of(MetaModel.class)
            .cacheable()
            .autoFlush(false)
            .filter("self.name = ?", entity.getClass().getSimpleName())
            .fetchOne());
    log.setTypeSelect(type);
    log.setUser(AuthUtils.getUser());
    log.setRelatedId(entity.getId());
    log.setRelatedReference(this.getRelatedReference(entity));
    log.setGlobalTrackingLogLineList(new ArrayList<>());

    return log;
  }

  protected String getRelatedReference(Model entity) {
    Mapper classMapper = Mapper.of(EntityHelper.getEntityClass(entity));

    if (classMapper.getNameField() != null && classMapper.getNameField().getName() != null) {
      String fieldName = classMapper.getNameField().getName();
      return (String) Mapper.toMap(entity).get(fieldName);
    }

    return "";
  }

  protected boolean isLineToBeAdded(
      GlobalTrackingLog log, GlobalTrackingConfigurationLine configLine) {
    ScriptBindings bindings = getScriptBinding(log);
    return configLine != null
        && this.canTrack(configLine, log.getTypeSelect())
        && (Strings.isNullOrEmpty(configLine.getTrackingCondition())
            || (!Strings.isNullOrEmpty(configLine.getTrackingCondition()))
                && Boolean.TRUE.equals(
                    new GroovyScriptHelper(bindings).eval(configLine.getTrackingCondition())));
  }

  protected ScriptBindings getScriptBinding(GlobalTrackingLog log) {
    ScriptBindings bindings = null;
    try {
      bindings =
          new ScriptBindings(
              this.getContext(
                  JPA.find(
                      (Class<Model>) Class.forName(log.getMetaModel().getFullName()),
                      log.getRelatedId())));
    } catch (Exception ignored) {
    }
    return bindings;
  }

  private Map<String, Object> getContext(Object obj)
      throws IntrospectionException,
          InvocationTargetException,
          IllegalAccessException,
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

  protected boolean canTrack(GlobalTrackingConfigurationLine confLine, int typeSelect) {
    return switch (typeSelect) {
      case GlobalTrackingLogRepository.TYPE_CREATE -> confLine.getTrackCreation();
      case GlobalTrackingLogRepository.TYPE_READ -> confLine.getTrackReading();
      case GlobalTrackingLogRepository.TYPE_UPDATE -> confLine.getTrackUpdate();
      case GlobalTrackingLogRepository.TYPE_DELETE -> confLine.getTrackDeletion();
      case GlobalTrackingLogRepository.TYPE_EXPORT -> confLine.getTrackExport();
      default -> false;
    };
  }

  private GlobalTrackingLogRepository getGlobalTrackingLogRepository() {
    if (globalTrackingLogRepository == null) {
      globalTrackingLogRepository = Beans.get(GlobalTrackingLogRepository.class);
    }
    return globalTrackingLogRepository;
  }

  protected List<GlobalTrackingConfigurationLine> getGlobalTrackingConfigLines(Model model) {
    return Query.of(GlobalTrackingConfigurationLine.class)
        .cacheable()
        .autoFlush(false)
        .filter("self.metaModel.fullName = ?", model.getClass().getName())
        .cacheable()
        .autoFlush(false)
        .fetch();
  }

  protected boolean isNotCollectionField(MetaField metaField) {
    String relationShip = metaField.getRelationship();
    return StringUtils.isEmpty(relationShip)
        || (relationShip.equals("OneToMany") && relationShip.equals("ManyToMany"));
  }

  private void processTracks() {
    var count = 0;

    for (var states : store.values()) {
      for (var state : states.values()) {
        process(state);

        if (++count % DBHelper.getJdbcBatchSize() == 0) {
          JPA.flush();
          JPA.clear();
        }
      }
    }
  }

  private void processCollections() {
    var count = 0;

    for (var collectionState : updatedCollections) {

      processCollection(collectionState);

      if (++count % DBHelper.getJdbcBatchSize() == 0) {
        JPA.flush();
        JPA.clear();
      }
    }
  }

  private void processCollection(CollectionState collectionState) {
    List<GlobalTrackingConfigurationLine> configurationLines =
        getGlobalTrackingConfigLines(collectionState.owner()).stream()
            .filter(
                configLine ->
                    configLine.getMetaField().getRelationship() != null
                        && (configLine.getMetaField().getRelationship().equals("OneToMany")
                            || configLine.getMetaField().getRelationship().equals("ManyToOne")))
            .toList();
    ;
    if (CollectionUtils.isEmpty(configurationLines)) {
      return;
    }

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
        addCollectionModification(collection, oldCollection, configLine.getMetaField());
      }
    }
  }

  protected void addCollectionModification(
      Object collection, Object oldCollection, MetaField metaField) {
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

    createLog(newValues, oldValues, metaField);
  }

  protected void createLog(
      AbstractPersistentCollection newValues,
      Collection<AuditableModel> oldValues,
      MetaField metaField) {
    Model owner = (Model) newValues.getOwner();
    String fieldName = getFieldName(newValues.getRole(), owner);

    GlobalTrackingLog log = createLog(owner, GlobalTrackingLogRepository.TYPE_UPDATE);

    List<Long> previousIdList = getPreviousIdList(oldValues);
    List<Long> newIdList = getNewIdList((Collection<AuditableModel>) newValues);
    createLineLog(log, fieldName, previousIdList, newIdList, metaField);
    getGlobalTrackingLogRepository().save(log);
  }

  protected void createLineLog(
      GlobalTrackingLog log,
      String fieldName,
      List<Long> previousIdList,
      List<Long> newIdList,
      MetaField metaField) {
    GlobalTrackingLogLine line =
        log.getGlobalTrackingLogLineList().stream()
            .filter(l -> l.getMetaFieldName().equals(fieldName))
            .findFirst()
            .orElse(null);

    if (line == null) {
      line = new GlobalTrackingLogLine();
      line.setMetaFieldName(fieldName);
      line.setMetaField(metaField);
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

  private void processDelete() {
    final MetaFiles files = Beans.get(MetaFiles.class);
    for (Model entity : deleted) {
      files.deleteAttachments(entity);
    }
  }

  public void addUpdatedCollection(Model owner, PersistentCollection<? extends Model> collection) {
    @SuppressWarnings("unchecked")
    var value = (Collection<? extends Model>) collection.getValue();

    if (collection.getStoredSnapshot() instanceof HashMap<?, ?>) {
      return;
    }

    @SuppressWarnings("unchecked")
    var snapshot = (Collection<? extends Model>) collection.getStoredSnapshot();

    if (snapshot == null || snapshot.isEmpty()) {
      return;
    }

    var oldValue =
        snapshot instanceof Set
            ? snapshot.stream().collect(Collectors.toSet())
            : snapshot.stream().toList();

    updatedCollections.add(new CollectionState(owner, value, oldValue));
  }

  private void fireBeforeCompleteEvent() {
    if (!updated.isEmpty() || !deleted.isEmpty()) {
      Beans.get(BeforeTransactionCompleteService.class).fire(updated, deleted);
    }
  }

  @Override
  public void doBeforeTransactionCompletion(SessionImplementor session) {
    fireBeforeCompleteEvent();

    processTracks();
    processDelete();
    processCollections();

    if (session.getHibernateFlushMode() == FlushMode.MANUAL || session.isClosed()) {
      return;
    }

    session.flush();
  }

  private static class EntityState {

    private Model entity;
    private Map<String, Object> values;
    private Map<String, Object> oldValues;
  }

  @Singleton
  static class BeforeTransactionCompleteService {

    @Inject private Event<BeforeTransactionComplete> event;

    public void fire(Set<? extends Model> updated, Set<? extends Model> deleted) {
      event.fire(new BeforeTransactionComplete(updated, deleted));
    }
  }
}
