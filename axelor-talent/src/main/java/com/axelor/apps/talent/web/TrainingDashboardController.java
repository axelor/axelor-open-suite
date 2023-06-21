package com.axelor.apps.talent.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.talent.service.TrainingDashboardService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.Map;

public class TrainingDashboardController {

  public void getTrainingData(ActionRequest request, ActionResponse response) {

    try {
      List<Map<String, Object>> trainingData =
          Beans.get(TrainingDashboardService.class).getTrainingData();
      response.setData(trainingData);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }
}
