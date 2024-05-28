package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.service.ProductReservationServiceImpl;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

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

    if (productReservation.getTypeSelect()
            == ProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION
        && reservationTypeSelect
            == ProductReservationRepository.PRODUCT_TYPE_OF_USAGE_TO_SELL_ON_MOVE_LINE) {
      return;
    }

    BigDecimal productReservationQty = productReservation.getQty();
    ManufOrder manufOrder = productReservation.getOriginManufOrder();

    if (reservationTypeSelect
            == ProductReservationRepository.PRODUCT_TYPE_OF_USAGE_TO_PRODUCE_ON_MANUF_ORDER
        && productReservationQty.compareTo(manufOrder.getQty()) <= 0) {
      productReservation.setStatus(
          ProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS);
    }

    BigDecimal toConsumeQty = BigDecimal.ZERO;
    Optional<ProdProduct> productToConsume =
        manufOrder.getToConsumeProdProductList().stream()
            .filter(it -> it.getProduct() == productReservation.getProduct())
            .findFirst();
    if (productToConsume.isPresent()) {
      toConsumeQty = productToConsume.get().getQty();
    }
    if (reservationTypeSelect
            == ProductReservationRepository.PRODUCT_TYPE_OF_USAGE_TO_CONSUME_ON_MANUF_ORDER
        && productReservationQty.compareTo(toConsumeQty) <= 0) {

      productReservation.setStatus(
          ProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS);
    }
  }
}
