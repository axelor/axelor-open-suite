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
import com.axelor.apps.base.db.TraceBack;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuditInterceptor;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.Role;
import com.axelor.mail.db.MailFlags;
import com.axelor.mail.db.MailFollower;
import com.axelor.mail.db.MailMessage;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.MetaView;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

@SuppressWarnings("serial")
public class GlobalAuditInterceptor extends AuditInterceptor {

  private static final String UPDATED_BY = "updatedBy";
  private static final String UPDATED_ON = "updatedOn";
  private static final String CREATED_BY = "createdBy";
  private static final String CREATED_ON = "createdOn";

  protected static final Class[] BACKLISTED_CLASSES = {
    GlobalTrackingLogLine.class,
    GlobalTrackingLog.class,
    GlobalTrackingConfigurationLine.class,
    MailMessage.class,
    MailFlags.class,
    MailFollower.class,
    MetaModel.class,
    MetaField.class,
    MetaModule.class,
    MetaView.class,
    MetaAction.class,
    MetaTranslation.class,
    MetaMenu.class,
    MetaSelect.class,
    MetaSelectItem.class,
    Group.class,
    Role.class,
    TraceBack.class
  };

  private final ThreadLocal<GlobalAuditTracker> globalTracker = new ThreadLocal<>();

  @Override
  public void afterTransactionBegin(Transaction tx) {
    globalTracker.set(new GlobalAuditTracker());
    globalTracker.get().init();
    super.afterTransactionBegin(tx);
  }

  @Override
  public void beforeTransactionCompletion(Transaction tx) {
    globalTracker.get().onComplete(tx, AuthUtils.getUser());
    super.beforeTransactionCompletion(tx);
  }

  @Override
  public void afterTransactionCompletion(Transaction tx) {
    globalTracker.get().clear();
    globalTracker.remove();
    super.afterTransactionCompletion(tx);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean onSave(
      Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

    if (!super.onSave(entity, id, state, propertyNames, types)
        || Arrays.asList(BACKLISTED_CLASSES).contains(entity.getClass())
        || !(entity instanceof AuditableModel)) {
      return false;
    }

    GlobalTrackingLog log =
        globalTracker
            .get()
            .addLog((AuditableModel) entity, GlobalTrackingLogRepository.TYPE_CREATE);

    for (int i = 0; i < propertyNames.length; i++) {
      if (state[i] == null
          || CREATED_ON.equals(propertyNames[i])
          || CREATED_BY.equals(propertyNames[i])) {
        continue;
      }

      GlobalTrackingLogLine logLine = new GlobalTrackingLogLine();
      logLine.setMetaFieldName(propertyNames[i]);

      if (state[i] instanceof AuditableModel) {
        logLine.setNewValue(String.valueOf(((AuditableModel) state[i]).getId()));
      } else if (state[i] instanceof Collection) {

        String newVal = "";
        if (CollectionUtils.isNotEmpty((Collection<Object>) state[i])) {
          newVal =
              String.format(
                  "[%s]",
                  ((Collection<AuditableModel>) state[i])
                      .stream()
                          .map(AuditableModel::getId)
                          .map(String::valueOf)
                          .collect(Collectors.joining(", ")));
        }
        logLine.setNewValue(newVal);
      } else {
        logLine.setNewValue(String.valueOf(Optional.ofNullable(state[i]).orElse("")));
      }

      log.addGlobalTrackingLogLineListItem(logLine);
    }
    if (log != null) {
      globalTracker.get().addLog(log);
    }

    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean onFlushDirty(
      Object entity,
      Serializable id,
      Object[] currentState,
      Object[] previousState,
      String[] propertyNames,
      Type[] types) {

    if (!super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types)
        || Arrays.asList(BACKLISTED_CLASSES).contains(entity.getClass())
        || !(entity instanceof AuditableModel)) {
      return false;
    }

    if (globalTracker.get() == null) {
      globalTracker.set(new GlobalAuditTracker());
      globalTracker.get().init();
    }
    GlobalTrackingLog log =
        globalTracker
            .get()
            .addLog((AuditableModel) entity, GlobalTrackingLogRepository.TYPE_UPDATE);

    for (int i = 0; i < propertyNames.length; i++) {

      if (Objects.equals(currentState[i], previousState[i])
          || UPDATED_ON.equals(propertyNames[i])
          || UPDATED_BY.equals(propertyNames[i])) {
        continue;
      }

      GlobalTrackingLogLine logLine = new GlobalTrackingLogLine();
      logLine.setMetaFieldName(propertyNames[i]);

      if (currentState[i] instanceof AuditableModel || previousState[i] instanceof AuditableModel) {

        logLine.setNewValue(
            currentState[i] instanceof AuditableModel
                ? String.valueOf(((AuditableModel) currentState[i]).getId())
                : "");
        logLine.setPreviousValue(
            previousState[i] instanceof AuditableModel
                ? String.valueOf(((AuditableModel) previousState[i]).getId())
                : "");

      } else if (currentState[i] instanceof Collection || previousState[i] instanceof Collection) {

        String prevVal = "";
        String newVal = "";
        if (CollectionUtils.isNotEmpty((Collection<Object>) previousState[i])) {
          prevVal =
              String.format(
                  "[%s]",
                  ((Collection<AuditableModel>) previousState[i])
                      .stream()
                          .map(AuditableModel::getId)
                          .map(String::valueOf)
                          .collect(Collectors.joining(", ")));
        }
        if (CollectionUtils.isNotEmpty((Collection<Object>) currentState[i])) {
          newVal =
              String.format(
                  "[%s]",
                  ((Collection<AuditableModel>) currentState[i])
                      .stream()
                          .map(AuditableModel::getId)
                          .map(String::valueOf)
                          .collect(Collectors.joining(", ")));
        }
        logLine.setPreviousValue(prevVal);
        logLine.setNewValue(newVal);

      } else {
        logLine.setNewValue(String.valueOf(Optional.ofNullable(currentState[i]).orElse("")));
        logLine.setPreviousValue(String.valueOf(Optional.ofNullable(previousState[i]).orElse("")));
      }

      log.addGlobalTrackingLogLineListItem(logLine);
    }

    return true;
  }

  @Override
  public void onDelete(
      Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
    super.onDelete(entity, id, state, propertyNames, types);
    if (entity instanceof AuditableModel
        && !Arrays.asList(BACKLISTED_CLASSES).contains(entity.getClass())) {
      globalTracker.get().addLog((AuditableModel) entity, GlobalTrackingLogRepository.TYPE_DELETE);
    }
  }

  @Override
  public boolean onLoad(
      Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

    // may be used to track reading
    return false;
  }

  @Override
  public void onCollectionUpdate(Object collection, Serializable key) {
    globalTracker.get().addCollectionModification(collection, (Long) key);
  }
}
