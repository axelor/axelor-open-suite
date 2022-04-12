package com.axelor.apps.supplychain.web;

import com.axelor.apps.supplychain.db.MrpForecast;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.service.MrpForecastService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MrpForecastController {

  public void confirm(ActionRequest request, ActionResponse response) {
    try {
      MrpForecast mrpForecast = request.getContext().asType(MrpForecast.class);
      mrpForecast = Beans.get(MrpForecastRepository.class).find(mrpForecast.getId());
      Beans.get(MrpForecastService.class).confirm(mrpForecast);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      MrpForecast mrpForecast = request.getContext().asType(MrpForecast.class);
      mrpForecast = Beans.get(MrpForecastRepository.class).find(mrpForecast.getId());
      Beans.get(MrpForecastService.class).cancel(mrpForecast);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
