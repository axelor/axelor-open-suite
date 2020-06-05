package com.axelor.apps.base.job;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.tracking.GlobalTrackingLogService;
import com.axelor.exception.service.TraceBackService;
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
      TraceBackService.trace(e);
      throw new UncheckedJobExecutionException(e);
    }
  }
}
