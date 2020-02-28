package com.axelor.apps.admin.service;

import com.axelor.apps.admin.db.GlobalTrackingLog;
import com.axelor.apps.admin.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class GlobalTrackingLogServiceImpl implements GlobalTrackingLogService {

  protected GlobalTrackingLogRepository globalTrackingLogRepo;
  protected MetaFileRepository metaFileRepo;

  @Inject
  public GlobalTrackingLogServiceImpl(
      GlobalTrackingLogRepository globalTrackingLogRepo, MetaFileRepository metaFileRepo) {
    this.globalTrackingLogRepo = globalTrackingLogRepo;
    this.metaFileRepo = metaFileRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public GlobalTrackingLog createExportLog(MetaModel model) {

    GlobalTrackingLog log = new GlobalTrackingLog();
    log.setDateT(LocalDateTime.now());
    log.setMetaModel(model);
    log.setTypeSelect(GlobalTrackingLogRepository.TYPE_EXPORT);
    log.setUser(AuthUtils.getUser());
    return globalTrackingLogRepo.save(log);
  }

  @Override
  public void deleteOldGlobalTrackingLog(int months) throws Exception {
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
  @Transactional(rollbackOn = {Exception.class})
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
}
