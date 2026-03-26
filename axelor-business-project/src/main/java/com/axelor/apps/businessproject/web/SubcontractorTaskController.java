package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.db.SubcontractorTask;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SubcontractorTaskController {

  public void syncTimeSpent(ActionRequest request, ActionResponse response) {
    SubcontractorTask task = request.getContext().asType(SubcontractorTask.class);

    BigDecimal minutes = task.getTimeSpentMinutes();
    BigDecimal hours =
        minutes != null
            ? minutes.divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    response.setValue("timeSpent", hours);
  }
}
