package com.axelor.apps.mobilesettings.web;

import com.axelor.apps.mobilesettings.db.MobileChart;
import com.axelor.apps.mobilesettings.service.MobileChartService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MobileChartController {
  public void getJsonResponse(ActionRequest request, ActionResponse response) {
    MobileChart mobileChart = request.getContext().asType(MobileChart.class);
    response.setValue(
        "$response",
        Beans.get(MobileChartService.class).getJsonResponse(mobileChart).toJSONString());
  }
}
