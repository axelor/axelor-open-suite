package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.service.YearService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class YearController {  

  public void generatePeriods(ActionRequest request, ActionResponse response)
      throws AxelorException {

    try {
      Year year = request.getContext().asType(Year.class);      
      response.setValue("periodList", Beans.get(YearService.class).generatePeriods(year));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
