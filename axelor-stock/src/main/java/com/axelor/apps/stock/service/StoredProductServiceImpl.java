package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StoredProductServiceImpl implements StoredProductService {

  protected StoredProductRepository storedProductRepository;
  protected MassStockMoveRepository massStockMoveRepository;
  protected StockMoveRepository stockMoveRepository;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;

  @Inject
  public StoredProductServiceImpl(
      StoredProductRepository storedProductRepository,
      MassStockMoveRepository massStockMoveRepository,
      StockMoveRepository stockMoveRepository,
      StockMoveLineRepository stockMoveLineRepository,
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService) {
    this.storedProductRepository = storedProductRepository;
    this.massStockMoveRepository = massStockMoveRepository;
    this.stockMoveRepository = stockMoveRepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
  }

  @Override
  public StoredProduct createStoredProduct(
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal currentQty,
      StockLocation toStockLocation,
      BigDecimal storedQty,
      StockMoveLine stockMoveLine,
      MassStockMove massStockMove) {
    StoredProduct storedProduct = new StoredProduct();
    storedProduct.setStoredProduct(product);
    storedProduct.setTrackingNumber(trackingNumber);
    storedProduct.setCurrentQty(currentQty);
    storedProduct.setUnit(product.getUnit());
    storedProduct.setToStockLocation(toStockLocation);
    storedProduct.setStoredQty(storedQty);
    storedProduct.setStockMoveLine(stockMoveLine);
    storedProduct.setMassStockMove(massStockMove);
    return storedProduct;
  }

  protected StoredProduct duplicateStoredProduct(StoredProduct storedProduct) {
    StoredProduct duplicatedStoredProduct = storedProductRepository.copy(storedProduct, false);
    duplicatedStoredProduct.setCurrentQty(
        storedProduct.getCurrentQty().subtract(storedProduct.getStoredQty()));
    duplicatedStoredProduct.setStoredQty(BigDecimal.ZERO);
    return duplicatedStoredProduct;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createStockMoveForStoredProduct(StoredProduct storedProduct)
      throws AxelorException {
    StockLocation toStockLocation = storedProduct.getToStockLocation();
    StockLocation fromStockLocation = storedProduct.getMassStockMove().getCartStockLocation();
    BigDecimal qty = storedProduct.getStoredQty();
    StockMove stockMove =
        stockMoveService.createStockMove(
            storedProduct.getMassStockMove().getCartStockLocation().getAddress(),
            storedProduct.getToStockLocation().getAddress(),
            storedProduct.getMassStockMove().getCompany(),
            fromStockLocation,
            toStockLocation,
            LocalDate.now(),
            LocalDate.now(),
            null,
            StockMoveRepository.TYPE_INTERNAL);
    StockMoveLine stockMoveLine =
        stockMoveLineService.createStockMoveLine(
            stockMove,
            storedProduct.getStoredProduct(),
            storedProduct.getTrackingNumber(),
            qty,
            qty,
            storedProduct.getUnit(),
            null,
            fromStockLocation,
            toStockLocation);
    stockMove.setMassStockMove(storedProduct.getMassStockMove());
    stockMoveService.plan(stockMove);
    stockMoveService.realize(stockMove);
    storedProduct.setStockMoveLine(stockMoveLine);
    storedProductRepository.save(storedProduct);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createStockMoveAndStockMoveLine(StoredProduct storedProduct) throws AxelorException {
    BigDecimal storedQty = storedProduct.getStoredQty();
    BigDecimal currentQty = storedProduct.getCurrentQty();

    if (storedQty.compareTo(currentQty) == -1 && storedQty.compareTo(BigDecimal.ZERO) != 0) {
      StoredProduct duplicatedStoredProduct = duplicateStoredProduct(storedProduct);
      storedProductRepository.save(duplicatedStoredProduct);
    }

    if (storedProduct.getStoredQty().compareTo(BigDecimal.ZERO) != 0
        && storedProduct.getToStockLocation() != null
        && storedProduct.getStockMoveLine() == null) {

      if (storedProduct.getStoredQty().compareTo(storedProduct.getCurrentQty()) == 1
          && storedProduct.getTrackingNumber() != null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(StockExceptionMessage.STORED_QUANTITY_GREATER_THAN_CURRENT_QTY),
            storedProduct.getStoredProduct().getFullName(),
            storedProduct.getTrackingNumber() != null
                ? storedProduct.getTrackingNumber().getTrackingNumberSeq()
                : "");
      }
      createStockMoveForStoredProduct(storedProduct);
    } else if (storedProduct.getStoredQty().compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.STORED_QUANTITY_IS_ZERO),
          storedProduct.getStoredProduct().getFullName(),
          storedProduct.getTrackingNumber() != null
              ? storedProduct.getTrackingNumber().getTrackingNumberSeq()
              : "");

    } else if (storedProduct.getStockMoveLine() != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.ALREADY_STORED_PRODUCT),
          storedProduct.getStoredProduct(),
          storedProduct.getTrackingNumber() != null
              ? storedProduct.getTrackingNumber().getTrackingNumberSeq()
              : "");
    }

    if (checkIfAllStoredProductAreStored(storedProduct.getMassStockMove())) {
      storedProduct.getMassStockMove().setStatusSelect(MassStockMoveRepository.STATUS_REALIZED);
      massStockMoveRepository.save(storedProduct.getMassStockMove());
    }
  }

  public boolean checkIfAllStoredProductAreStored(MassStockMove massStockMove) {
    return massStockMove.getStoredProductList().stream()
        .anyMatch(
            it ->
                !(it.getStoredQty().compareTo(it.getCurrentQty()) == 0
                    && it.getStockMoveLine() != null));
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void cancelStockMoveAndStockMoveLine(StoredProduct storedProduct) throws AxelorException {
    if (storedProduct.getStockMoveLine() != null) {
      stockMoveService.cancel(storedProduct.getStockMoveLine().getStockMove());
      storedProduct.setStockMoveLine(null);
      storedProduct.setStoredQty(BigDecimal.ZERO);
      storedProductRepository.save(storedProduct);
    }
  }
}
