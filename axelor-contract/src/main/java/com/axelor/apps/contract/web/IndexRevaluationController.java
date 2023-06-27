package com.axelor.apps.contract.web;

import com.axelor.apps.contract.db.IndexRevaluation;
import com.axelor.apps.contract.service.IndexRevaluationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class IndexRevaluationController {

  public void setIndexValuesEndDate(ActionRequest request, ActionResponse response) {
    IndexRevaluation indexRevaluation = request.getContext().asType(IndexRevaluation.class);
    Beans.get(IndexRevaluationService.class).setIndexValueEndDate(indexRevaluation);
    response.setValues(indexRevaluation);
  }
}
