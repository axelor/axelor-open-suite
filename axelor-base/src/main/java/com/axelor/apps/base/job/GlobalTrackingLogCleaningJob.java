package com.axelor.apps.base.job;

import com.axelor.apps.admin.service.GlobalTrackingLogService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import org.quartz.JobExecutionContext;

public class GlobalTrackingLogCleaningJob extends ThreadedJob {

  @Override
  public void executeInThread(JobExecutionContext context) {
    try {
      Beans.get(GlobalTrackingLogService.class)
          .deleteOldGlobalTrackingLog(
              Beans.get(AppBaseService.class).getGlobalTrackingLogPersistence());
    } catch (Exception e) {
      throw new UncheckedJobExecutionException(e);
    }
  }
}
