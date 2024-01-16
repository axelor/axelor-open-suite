package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ProductReservationServiceImpl implements ProductReservationService {

  protected ProductReservationRepository productReservationRepository;
  protected StockLocationService stockLocationService;

  @Inject
  public ProductReservationServiceImpl(
      ProductReservationRepository productReservationRepository,
      StockLocationService stockLocationService) {
    this.productReservationRepository = productReservationRepository;
    this.stockLocationService = stockLocationService;
  }

  @Override
  public ProductReservation createProductReservation(
      Product product,
      BigDecimal qty,
      int typeSelect,
      StockLocation stockLocation,
      TrackingNumber trackingNumber) {
    ProductReservation productReservation = new ProductReservation();
    productReservation.setProduct(product);
    productReservation.setStockLocation(stockLocation);
    productReservation.setTrackingNumber(trackingNumber);
    productReservation.setQty(qty);
    productReservation.setTypeSelect(typeSelect);
    return productReservation;
  }

  @Override
  @Transactional
  public void cancelReservation(ProductReservation productReservation) {
    productReservation.setStatus(ProductReservationRepository.PRODUCT_RESERVATION_STATUS_CANCELED);
    productReservationRepository.save(productReservation);
  }

  @Override
  @Transactional
  public ProductReservation updateStatus(ProductReservation productReservation)
      throws AxelorException {
    if (productReservation.getTypeSelect()
        == ProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION) {
      productReservation.setStatus(
          ProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS);
    } else if (productReservation.getTypeSelect()
        == ProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION) {
      BigDecimal availableQty = this.getAvailableQty(productReservation);

      if (productReservation.getQty().compareTo(availableQty) < 0) {
        productReservation.setStatus(
            ProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS);
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                ((productReservation.getProduct().getTrackingNumberConfiguration() != null)
                    ? SupplychainExceptionMessage.ALLOCATION_QTY_BY_TRACKING_NUMBER_IS_NOT_AVAILABLE
                    : SupplychainExceptionMessage.ALLOCATION_QTY_BY_PRODUCT_IS_NOT_AVAILABLE)),
            productReservation);
      }
    }
    return productReservation;
  }

  public BigDecimal getAvailableQty(ProductReservation productReservation) throws AxelorException {
    BigDecimal alreadyAllocatedQty =
        this.getAllocatedQty(
            productReservation.getProduct(),
            productReservation.getStockLocation(),
            productReservation.getProduct().getTrackingNumberConfiguration() != null
                ? productReservation.getTrackingNumber()
                : null);

    BigDecimal realQty = this.getRealQtyForProductReservation(productReservation);
    return realQty.subtract(alreadyAllocatedQty).setScale(2, RoundingMode.HALF_UP);
  }

  public BigDecimal getAllocatedQty(
      Product product, StockLocation stockLocation, TrackingNumber trackingNumber) {
    return productReservationRepository
        .all()
        .filter(
            "self.typeSelect = :typeSelect AND self.status = :status"
                + (stockLocation != null ? " AND self.stockLocation = :stockLocation" : "")
                + (trackingNumber != null
                    ? " AND self.trackingNumber = :trackingNumber"
                    : " AND self.product = :product"))
        .bind("product", product)
        .bind("typeSelect", ProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION)
        .bind("status", ProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS)
        .bind("stockLocation", stockLocation)
        .bind("trackingNumber", trackingNumber)
        .fetchStream()
        .map(ProductReservation::getQty)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal getAvailableQtyForAllocation(Product product, StockLocation stockLocation)
      throws AxelorException {
    List<Long> stockLocationIds = new ArrayList<Long>();
    if (stockLocation != null) {
      stockLocationIds.add(stockLocation.getId());
    }
    return stockLocationService
        .getRealQtyOfProductInStockLocations(product.getId(), stockLocationIds, null)
        .subtract(this.getAllocatedQty(product, stockLocation, null));
  }

  protected BigDecimal getRealQtyForProductReservation(ProductReservation productReservation)
      throws AxelorException {
    BigDecimal realQty = BigDecimal.ZERO;
    if (productReservation.getProduct() != null) {
      List<Long> stockLocationIds = new ArrayList<Long>();
      if (productReservation.getStockLocation() != null) {
        stockLocationIds.add(productReservation.getStockLocation().getId());
      }
      realQty =
          stockLocationService.getRealQtyOfProductInStockLocations(
              productReservation.getProduct().getId(), stockLocationIds, null);
    }
    return realQty;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void realizeProductReservation(
      ProductReservation productReservation, StockLocation stockLocation) {
    if (stockLocation.getTypeSelect() == StockLocationRepository.TYPE_VIRTUAL) {
      productReservation.setStatus(
          ProductReservationRepository.PRODUCT_RESERVATION_STATUS_REALIZED);
    } else {
      productReservation.setStockLocation(stockLocation);
    }
    productReservationRepository.save(productReservation);
  }
}
