package com.axelor.apps.admin.service;

import com.axelor.apps.admin.db.GlobalTrackingLog;
import com.axelor.apps.admin.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.meta.db.MetaModel;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;

public class GlobalTrackingLogServiceImpl implements GlobalTrackingLogService {

  protected GlobalTrackingLogRepository globalTrackingLogRepo;

  @Inject
  public GlobalTrackingLogServiceImpl(GlobalTrackingLogRepository globalTrackingLogRepo) {
    this.globalTrackingLogRepo = globalTrackingLogRepo;
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
}
