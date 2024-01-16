package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.supplychain.db.ProductReservation;
import java.math.BigDecimal;

public interface ProductReservationService {
  ProductReservation createProductReservation(
      Product product,
      BigDecimal qty,
      int typeSelect,
      StockLocation stockLocation,
      TrackingNumber trackingNumber);

  ProductReservation updateStatus(ProductReservation productReservation) throws AxelorException;

  void cancelReservation(ProductReservation productReservation);

  BigDecimal getAvailableQty(ProductReservation productReservation) throws AxelorException;

  BigDecimal getAllocatedQty(
      Product product, StockLocation stockLocation, TrackingNumber trackingNumber);

  BigDecimal getAvailableQtyForAllocation(Product product, StockLocation stockLocation)
      throws AxelorException;

  void realizeProductReservation(
      ProductReservation productReservation, StockLocation stockLocation);
}
