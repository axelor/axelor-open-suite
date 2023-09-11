package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.StoredProductsRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StoredProductsServiceImpl implements StoredProductsService {

  protected StoredProductsRepository storeProductsRepository;
  protected MassStockMoveRepository massStockMoveRepository;
  protected StockMoveRepository stockMoveRepository;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;

  @Inject
  public StoredProductsServiceImpl(
      StoredProductsRepository storeProductsRepository,
      MassStockMoveRepository massStockMoveRepository,
      StockMoveRepository stockMoveRepository,
      StockMoveLineRepository stockMoveLineRepository,
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService) {
    this.storeProductsRepository = storeProductsRepository;
    this.massStockMoveRepository = massStockMoveRepository;
    this.stockMoveRepository = stockMoveRepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
  }

  @Override
  public StoredProducts createStoredProduct(
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal currentQty,
      StockLocation toStockLocation,
      BigDecimal storedQty,
      StockMoveLine stockMoveLine,
      MassStockMove massStockMove) {
    StoredProducts storedProducts = new StoredProducts();
    storedProducts.setStoredProduct(product);
    storedProducts.setTrackingNumber(trackingNumber);
    storedProducts.setCurrentQty(currentQty);
    storedProducts.setUnit(product.getUnit());
    storedProducts.setToStockLocation(toStockLocation);
    storedProducts.setStoredQty(storedQty);
    storedProducts.setStockMoveLine(stockMoveLine);
    storedProducts.setMassStockMove(massStockMove);
    return storedProducts;
  }

  protected StoredProducts duplicateStoredProduct(StoredProducts storedProduct) {
    StoredProducts duplicatedStoredProduct = storeProductsRepository.copy(storedProduct, false);
    duplicatedStoredProduct.setCurrentQty(
        storedProduct.getCurrentQty().subtract(storedProduct.getStoredQty()));
    duplicatedStoredProduct.setStoredQty(BigDecimal.ZERO);
    return duplicatedStoredProduct;
  }

  @Transactional
  protected void createStockMoveForStoredProducts(StoredProducts storedProducts)
      throws AxelorException {
    StockLocation toStockLocation = storedProducts.getToStockLocation();
    StockLocation fromStockLocation = storedProducts.getMassStockMove().getCartStockLocation();
    BigDecimal qty = storedProducts.getStoredQty();
    StockMove stockMove =
        stockMoveService.createStockMove(
            storedProducts.getMassStockMove().getCartStockLocation().getAddress(),
            storedProducts.getToStockLocation().getAddress(),
            storedProducts.getMassStockMove().getCompany(),
            fromStockLocation,
            toStockLocation,
            LocalDate.now(),
            LocalDate.now(),
            null,
            StockMoveRepository.TYPE_INTERNAL);
    StockMoveLine stockMoveLine =
        stockMoveLineService.createStockMoveLine(
            stockMove,
            storedProducts.getStoredProduct(),
            storedProducts.getTrackingNumber(),
            qty,
            qty,
            storedProducts.getUnit(),
            null,
            fromStockLocation,
            toStockLocation);
    stockMove.setMassStockMove(storedProducts.getMassStockMove());
    stockMoveService.plan(stockMove);
    stockMoveService.realize(stockMove);
    storedProducts.setStockMoveLine(stockMoveLine);
    storeProductsRepository.save(storedProducts);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createStockMoveAndStockMoveLine(StoredProducts storedProducts)
      throws AxelorException {
    BigDecimal storedQty = storedProducts.getStoredQty();
    BigDecimal currentQty = storedProducts.getCurrentQty();

    if (storedQty.compareTo(currentQty) == -1 && storedQty.compareTo(BigDecimal.ZERO) != 0) {
      StoredProducts duplicatedStoredProduct = duplicateStoredProduct(storedProducts);
      storeProductsRepository.save(duplicatedStoredProduct);
    }

    if (storedProducts.getStoredQty().compareTo(BigDecimal.ZERO) != 0
        && storedProducts.getToStockLocation() != null
        && storedProducts.getStockMoveLine() == null) {

      if (storedProducts.getStoredQty().compareTo(storedProducts.getCurrentQty()) == 1
          && storedProducts.getTrackingNumber() != null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(StockExceptionMessage.STORED_QUANTITY_GREATER_THAN_CURRENT_QTY),
            storedProducts.getStoredProduct().getFullName(),
            storedProducts.getTrackingNumber() != null
                ? storedProducts.getTrackingNumber().getTrackingNumberSeq()
                : "");
      }
      createStockMoveForStoredProducts(storedProducts);
    } else if (storedProducts.getStoredQty().compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.STORED_QUANTITY_IS_ZERO),
          storedProducts.getStoredProduct().getFullName(),
          storedProducts.getTrackingNumber() != null
              ? storedProducts.getTrackingNumber().getTrackingNumberSeq()
              : "");

    } else if (storedProducts.getStockMoveLine() != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.ALREADY_STORED_PRODUCT),
          storedProducts.getStoredProduct(),
          storedProducts.getTrackingNumber() != null
              ? storedProducts.getTrackingNumber().getTrackingNumberSeq()
              : "");
    }

    if (checkIfAllStoredProductAreStored(storedProducts.getMassStockMove())) {
      storedProducts.getMassStockMove().setStatusSelect(MassStockMoveRepository.STATUS_REALIZED);
      massStockMoveRepository.save(storedProducts.getMassStockMove());
    }
  }

  public boolean checkIfAllStoredProductAreStored(MassStockMove massStockMove) {
    return massStockMove.getStoredProductsList().stream()
        .anyMatch(
            it ->
                !(it.getStoredQty().compareTo(it.getCurrentQty()) == 0
                    && it.getStockMoveLine() != null));
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void cancelStockMoveAndStockMoveLine(StoredProducts storedProducts)
      throws AxelorException {
    if (storedProducts.getStockMoveLine() != null) {
      stockMoveService.cancel(storedProducts.getStockMoveLine().getStockMove());
      storedProducts.setStockMoveLine(null);
      storedProducts.setStoredQty(BigDecimal.ZERO);
      storeProductsRepository.save(storedProducts);
    }
  }
}
