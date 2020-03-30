package com.axelor.apps.base.web;

import com.axelor.apps.admin.service.GlobalTrackingLogService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GlobalTrackingConfigController {
  public void cleanGlobalTrackingLogs(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(GlobalTrackingLogService.class)
          .deleteOldGlobalTrackingLog(
              Beans.get(AppBaseService.class).getGlobalTrackingLogPersistence());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
