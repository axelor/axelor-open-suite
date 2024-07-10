package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.service.ProductReservationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductReservationController {

  public void updateStatus(ActionRequest request, ActionResponse response) {
    try {
      ProductReservation productReservation = request.getContext().asType(ProductReservation.class);

      productReservation =
          Beans.get(ProductReservationService.class).updateStatus(productReservation);
      response.setValue("status", productReservation.getStatus());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelReservation(ActionRequest request, ActionResponse response) {
    try {
      ProductReservation productReservation = request.getContext().asType(ProductReservation.class);
      productReservation =
          Beans.get(ProductReservationRepository.class).find(productReservation.getId());

      Beans.get(ProductReservationService.class).cancelReservation(productReservation);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
