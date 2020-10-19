package com.axelor.apps.production.web;

import com.axelor.apps.production.db.Sop;
import com.axelor.apps.production.db.repo.SopRepository;
import com.axelor.apps.production.service.SopService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SopController {
  public void generateSOPLines(ActionRequest request, ActionResponse response) {
    Sop sop = request.getContext().asType(Sop.class);
    if (sop.getYear() == null) {
      response.setError("Please specify the year to generate lines.");
    } else {
      try {
        sop = Beans.get(SopRepository.class).find(sop.getId());
        Beans.get(SopService.class).generateSOPLines(sop);
        response.setReload(true);
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
  }
}
