package com.axelor.apps.production.service;

import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.service.ProductReservationServiceImpl;
import com.google.inject.Inject;

public class ProductReservationProductionServiceImpl extends ProductReservationServiceImpl {

  @Inject
  public ProductReservationProductionServiceImpl(
      ProductReservationRepository productReservationRepository,
      StockLocationService stockLocationService) {
    super(productReservationRepository, stockLocationService);
  }

  @Override
  public void unlink(ProductReservation productReservation) {
    productReservation.setOriginManufOrder(null);
    super.unlink(productReservation);
  }
}
