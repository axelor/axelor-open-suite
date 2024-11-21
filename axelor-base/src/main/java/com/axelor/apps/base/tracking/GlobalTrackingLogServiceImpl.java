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

import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.Inflector;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalTrackingLogServiceImpl implements GlobalTrackingLogService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected GlobalTrackingLogRepository globalTrackingLogRepo;
  protected MetaFileRepository metaFileRepo;

  @Inject
  public GlobalTrackingLogServiceImpl(
      GlobalTrackingLogRepository globalTrackingLogRepo, MetaFileRepository metaFileRepo) {
    this.globalTrackingLogRepo = globalTrackingLogRepo;
    this.metaFileRepo = metaFileRepo;
  }

  @Override
  @Transactional
  public GlobalTrackingLog createExportLog(MetaModel model, MetaFile metaFile) {

    GlobalTrackingLog log = new GlobalTrackingLog();
    log.setDateT(LocalDateTime.now());
    log.setMetaModel(model);
    log.setTypeSelect(GlobalTrackingLogRepository.TYPE_EXPORT);
    log.setUser(AuthUtils.getUser());
    log.setMetaFile(metaFile);
    return globalTrackingLogRepo.save(log);
  }

  @Override
  public void deleteOldGlobalTrackingLog(int months) {
    final int FETCH_LIMIT = 5;
    final Query<GlobalTrackingLog> query =
        globalTrackingLogRepo
            .all()
            .filter("self.createdOn <= :dateLimit")
            .bind("dateLimit", LocalDateTime.now().minusMonths(months));

    for (List<GlobalTrackingLog> globalTrackingLogList;
        !(globalTrackingLogList = query.fetch(FETCH_LIMIT)).isEmpty(); ) {
      removeGlobalTrackingLogs(globalTrackingLogList);
      JPA.clear();
    }
  }

  @Override
  @Transactional
  public void removeGlobalTrackingLogs(List<GlobalTrackingLog> globalTrackingLogList) {
    for (GlobalTrackingLog globalTrackingLog : globalTrackingLogList) {
      MetaFile metaFile = globalTrackingLog.getMetaFile();
      globalTrackingLogRepo.remove(globalTrackingLog);

      if (globalTrackingLog.getMetaFile() != null) {
        File metaFileFile = new File(globalTrackingLog.getMetaFile().getFilePath());
        metaFileFile.delete();
        metaFileRepo.remove(metaFile);
      }
    }
    JPA.flush();
  }

  @Override
  public ActionView.ActionViewBuilder createReferenceView(GlobalTrackingLog globalTrackingLog) {
    if (globalTrackingLog == null) {
      return null;
    }
    Class<?> modelClass = JPA.model(globalTrackingLog.getMetaModel().getFullName());
    final Inflector inflector = Inflector.getInstance();
    String viewName = inflector.dasherize(modelClass.getSimpleName());

    LOG.debug("Showing Tracking Log reference ::: {}", viewName);

    ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Reference"));
    actionViewBuilder.model(globalTrackingLog.getMetaModel().getFullName());

    if (globalTrackingLog.getRelatedId() != null) {
      actionViewBuilder.context("_showRecord", globalTrackingLog.getRelatedId());
    } else {
      actionViewBuilder.add("grid", String.format("%s-grid", viewName));
    }

    actionViewBuilder.add("form", String.format("%s-form", viewName));

    return actionViewBuilder;
  }
}
