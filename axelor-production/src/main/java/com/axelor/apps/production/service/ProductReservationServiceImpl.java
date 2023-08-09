package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ProductReservation;
import com.axelor.apps.production.db.repo.ProductReservationRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class ProductReservationServiceImpl implements ProductReservationService {

  public static final String REAL = "real";
  protected ProductReservationRepository productReservationRepository;

  @Inject
  public ProductReservationServiceImpl(ProductReservationRepository productReservationRepository) {
    this.productReservationRepository = productReservationRepository;
  }

  @Override
  public void updateStatus(ProductReservation productReservation) throws AxelorException {
    if (Boolean.TRUE.equals(productReservation.getIsReservation())) {
      updateStatusReservation(productReservation);
    } else if (Boolean.TRUE.equals(productReservation.getIsAllocation())) {
      updateStatusAllocation(productReservation);
    }
    saveProductReservation(productReservation);
  }

  protected void updateStatusReservation(ProductReservation productReservation) {
    productReservation.setProductReservationStatus(
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
      productReservation.setProductReservationStatus(
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
    BigDecimal realQty = getRealQty(productReservation);
    return realQty.subtract(alreadyAllocatedQty);
  }

  protected BigDecimal getRealQty(ProductReservation productReservation) throws AxelorException {
    return Beans.get(StockLocationService.class)
        .getQty(
            productReservation.getProduct().getId(),
            productReservation.getStockLocation().getId(),
            productReservation.getStockLocation().getCompany().getId(),
            REAL);
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

  protected void saveProductReservation(ProductReservation productReservationSourceModified) {
    ProductReservation productReservationEntityTarget =
        findOrInitProductReservationEntityTarget(productReservationSourceModified);
    update(productReservationSourceModified, productReservationEntityTarget);
    JPA.runInTransaction(() -> productReservationRepository.save(productReservationEntityTarget));
  }

  protected ProductReservation findOrInitProductReservationEntityTarget(
      ProductReservation productReservation) {
    if ((productReservation == null) || (productReservation.getId() == null)) {
      return new ProductReservation();
    }
    return productReservationRepository.findByIds(List.of(productReservation.getId())).stream()
        .findFirst()
        .orElse(new ProductReservation());
  }

  protected void update(
      ProductReservation productReservationSource, ProductReservation productReservationTarget) {
    productReservationTarget.setProduct(productReservationSource.getProduct());
    productReservationTarget.setQty(productReservationSource.getQty());
    productReservationTarget.setProductReservationType(
        productReservationSource.getProductReservationType());
    productReservationTarget.setDescription(productReservationSource.getDescription());
    productReservationTarget.setProductReservationStatus(
        productReservationSource.getProductReservationStatus());
    productReservationTarget.setOriginManufOrder(productReservationSource.getOriginManufOrder());
    productReservationTarget.setOriginSaleOrderLine(
        productReservationSource.getOriginSaleOrderLine());
    productReservationTarget.setPriorityReservationDateTime(
        productReservationSource.getPriorityReservationDateTime());
    productReservationTarget.setTrackingNumber(productReservationSource.getTrackingNumber());
    productReservationTarget.setRequestedReservedType(
        productReservationSource.getRequestedReservedType());
    productReservationTarget.setStockLocation(productReservationSource.getStockLocation());
  }
}
