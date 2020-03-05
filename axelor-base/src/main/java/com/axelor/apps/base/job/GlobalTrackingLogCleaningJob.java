package com.axelor.apps.base.job;

import org.quartz.JobExecutionContext;

import com.axelor.apps.admin.service.GlobalTrackingLogService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;

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
