package com.axelor.apps.crm.web;

import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.crm.db.CallForTenders;
import com.axelor.apps.crm.db.repo.CallForTendersRepository;
import com.axelor.apps.crm.service.CallForTendersService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;

public class CallForTendersController {

  public void win(ActionRequest request, ActionResponse response) {
    try {

      CallForTenders call =
          Beans.get(CallForTendersRepository.class)
              .find(request.getContext().asType(CallForTenders.class).getId());
      Beans.get(CallForTendersService.class).win(call);
      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void loose(ActionRequest request, ActionResponse response) {
    try {

      CallForTenders call =
          Beans.get(CallForTendersRepository.class)
              .find(request.getContext().asType(CallForTenders.class).getId());
      Beans.get(CallForTendersService.class).loose(call);
      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void setBackInProgress(ActionRequest request, ActionResponse response) {
    try {

      CallForTenders call =
          Beans.get(CallForTendersRepository.class)
              .find(request.getContext().asType(CallForTenders.class).getId());
      Beans.get(CallForTendersService.class).setBackInProgress(call);
      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void setEndDateConductivity(ActionRequest request, ActionResponse response) {
    CallForTenders call = request.getContext().asType(CallForTenders.class);
    if (call.getRolloverDuration() != null && call.getEndDate() != null) {
      LocalDate date =
          Beans.get(DurationService.class)
              .computeDuration(call.getRolloverDuration(), call.getEndDate());
      response.setValue("conductivityEndDate", date);
    }
  }
}
