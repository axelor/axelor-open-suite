package com.axelor.apps.production.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ProductReservation;
import com.axelor.apps.production.service.ProductReservationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductReservationController {

  public void updateStatus(ActionRequest request, ActionResponse response) {
    try {
      ProductReservation productionReservation =
          request.getContext().asType(ProductReservation.class);
      Beans.get(ProductReservationService.class).updateStatus(productionReservation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
