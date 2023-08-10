package com.axelor.apps.production.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ProductReservation;
import com.axelor.apps.production.db.repo.ProductReservationRepository;
import com.axelor.apps.production.service.ProductReservationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductReservationController {

  public void updateStatus(ActionRequest request, ActionResponse response) {
    try {
      ProductReservation productionReservationBuddy =
          request.getContext().asType(ProductReservation.class);
      ProductReservation productReservation =
          Beans.get(ProductReservationRepository.class).find(productionReservationBuddy.getId());
      Beans.get(ProductReservationService.class).updateStatus(productReservation);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
