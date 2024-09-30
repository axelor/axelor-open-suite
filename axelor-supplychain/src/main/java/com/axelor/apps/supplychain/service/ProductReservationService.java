package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.ProductReservation;

public interface ProductReservationService {
  ProductReservation updateStatus(ProductReservation productReservation) throws AxelorException;

  void cancelReservation(ProductReservation productReservation);
}
