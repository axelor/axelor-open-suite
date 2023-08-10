package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ProductReservation;
import com.axelor.apps.production.db.repo.ProductReservationRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class ProductReservationServiceImpl implements ProductReservationService {

  protected ProductReservationRepository productReservationRepository;

  @Inject
  public ProductReservationServiceImpl(ProductReservationRepository productReservationRepository) {
    this.productReservationRepository = productReservationRepository;
  }

  @Override
  @Transactional
  public void updateStatus(ProductReservation productReservation) throws AxelorException {
    if (Boolean.TRUE.equals(productReservation.getIsReservation())) {
      updateStatusReservation(productReservation);
    } else if (Boolean.TRUE.equals(productReservation.getIsAllocation())) {
      updateStatusAllocation(productReservation);
    }
    productReservationRepository.save(productReservation);
  }

  protected void updateStatusReservation(ProductReservation productReservation) {
    productReservation.setStatus(
        ProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS);
  }

  protected void updateStatusAllocation(ProductReservation productReservation)
      throws AxelorException {
    if (productReservation.getProduct().getTrackingNumberConfiguration() == null) {
      updateStatusAllocationByProduct(productReservation);
    } else {
      updateStatusAllocationByTrackingNumber(productReservation);
    }
  }

  protected void updateStatusAllocationByTrackingNumber(ProductReservation productReservation)
      throws AxelorException {
    updateStatusAllocation(
        computeAvailableQuantityForTrackingNumber(productReservation),
        productReservation,
        ProductionExceptionMessage.ALLOCATION_QTY_BY_TRACKING_NUMBER_IS_NOT_AVAILABLE);
  }

  protected void updateStatusAllocationByProduct(ProductReservation productReservation)
      throws AxelorException {
    updateStatusAllocation(
        computeAvailableQuantityForProduct(productReservation),
        productReservation,
        ProductionExceptionMessage.ALLOCATION_QTY_BY_PRODUCT_IS_NOT_AVAILABLE);
  }

  protected void updateStatusAllocation(
      BigDecimal availableQty,
      ProductReservation productReservation,
      String exceptionMessageOnNotAvailableQty)
      throws AxelorException {

    if (productReservation.getQty().compareTo(availableQty) <= 0) {
      productReservation.setStatus(
          ProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(exceptionMessageOnNotAvailableQty),
          productReservation);
    }
  }

  protected BigDecimal computeAvailableQuantityForTrackingNumber(
      ProductReservation productReservation) throws AxelorException {
    BigDecimal alreadyAllocatedQty =
        productReservationRepository
            .findByProductReservationTypeAndStockLocationAndTrackingNumber(
                productReservation.getProductReservationType(),
                productReservation.getStockLocation(),
                productReservation.getTrackingNumber())
            .fetchStream()
            .map(ProductReservation::getQty)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal realQty =
        Beans.get(StockLocationService.class)
            .getRealQty(
                productReservation.getProduct().getId(),
                productReservation.getStockLocation().getId(),
                productReservation.getStockLocation().getCompany().getId());
    return realQty.subtract(alreadyAllocatedQty);
  }

  protected BigDecimal computeAvailableQuantityForProduct(ProductReservation productReservation) {
    return productReservationRepository
        .findByProductReservationTypeAndStockLocationAndProduct(
            productReservation.getProductReservationType(),
            productReservation.getStockLocation(),
            productReservation.getProduct())
        .fetchStream()
        .map(ProductReservation::getQty)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
