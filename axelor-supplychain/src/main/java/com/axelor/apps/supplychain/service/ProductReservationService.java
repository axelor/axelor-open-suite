package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.ProductReservation;
import java.math.BigDecimal;

public interface ProductReservationService {
  ProductReservation updateStatus(ProductReservation productReservation) throws AxelorException;

  void cancelReservation(ProductReservation productReservation);

  BigDecimal getAvailableQty(ProductReservation productReservation) throws AxelorException;
}
