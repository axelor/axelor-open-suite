package com.axelor.apps.production.service;

import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.service.ProductReservationServiceImpl;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class ProductionProductReservationServiceImpl extends ProductReservationServiceImpl {

  @Inject
  public ProductionProductReservationServiceImpl(
      ProductReservationRepository productReservationRepository,
      StockLocationService stockLocationService) {
    super(productReservationRepository, stockLocationService);
  }

  @Override
  protected void updateReservationStatus(ProductReservation productReservation) {
    super.updateReservationStatus(productReservation);

    int reservationTypeSelect = Integer.parseInt(productReservation.getTypeOfUsageTypeSelect());
    BigDecimal productReservationQty = productReservation.getQty();
    if ((reservationTypeSelect
                == ProductReservationRepository.PRODUCT_TYPE_OF_USAGE_TO_PRODUCE_ON_MANUF_ORDER
            || reservationTypeSelect
                == ProductReservationRepository.PRODUCT_TYPE_OF_USAGE_TO_CONSUME_ON_MANUF_ORDER)
        && productReservationQty.compareTo(productReservation.getOriginManufOrder().getQty())
            <= 0) {
      productReservation.setStatus(
          ProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS);
    }
  }
}
