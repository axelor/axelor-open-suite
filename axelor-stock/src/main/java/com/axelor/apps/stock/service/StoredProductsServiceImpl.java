package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
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

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createStockMoveAndStockMoveLine(StoredProducts storedProducts)
      throws AxelorException {
    BigDecimal storedQty =
        storedProducts.getStoredQty() != null ? storedProducts.getStoredQty() : null;
    BigDecimal currentQty =
        storedProducts.getCurrentQty() != null ? storedProducts.getCurrentQty() : null;

    if (storedQty.compareTo(currentQty) == -1 && storedQty.compareTo(BigDecimal.ZERO) != 0) {
      Product duplicateProduct = storedProducts.getStoredProduct();
      TrackingNumber duplicateTrackingNumber = storedProducts.getTrackingNumber();
      StockLocation duplicateToStockLocation = storedProducts.getToStockLocation();
      StockMoveLine duplicateStockMoveLine = storedProducts.getStockMoveLine();
      MassStockMove duplicateMassStockMove = storedProducts.getMassStockMove();
      storedProducts.setCurrentQty(storedQty);
      StoredProducts duplicateStoredProduct =
          createStoredProduct(
              duplicateProduct,
              duplicateTrackingNumber,
              currentQty.subtract(storedQty),
              duplicateToStockLocation,
              BigDecimal.ZERO,
              duplicateStockMoveLine,
              duplicateMassStockMove);
      storeProductsRepository.save(duplicateStoredProduct);
    }

    if (storedProducts.getStoredQty().compareTo(BigDecimal.ZERO) != 0
        && storedProducts.getToStockLocation() != null
        && storedProducts.getStockMoveLine() == null) {

      if (storedProducts.getStoredQty().compareTo(storedProducts.getCurrentQty()) == 1
          && storedProducts.getTrackingNumber() != null) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.STORED_QUANTITY_GREATER_THAN_CURRENT_QTY)
                + " "
                + storedProducts.getStoredProduct().getFullName()
                + " "
                + storedProducts.getTrackingNumber().getTrackingNumberSeq());
      } else if (storedProducts.getStoredQty().compareTo(storedProducts.getCurrentQty()) == 1
          && storedProducts.getTrackingNumber() == null) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.STORED_QUANTITY_GREATER_THAN_CURRENT_QTY)
                + " "
                + storedProducts.getStoredProduct().getFullName());
      }
      Address toAddress = storedProducts.getToStockLocation().getAddress();
      Address fromAddress = storedProducts.getMassStockMove().getCartStockLocation().getAddress();
      Company company = storedProducts.getMassStockMove().getCompany();
      StockLocation toStockLocation = storedProducts.getToStockLocation();
      StockLocation fromStockLocation = storedProducts.getMassStockMove().getCartStockLocation();
      Product product = storedProducts.getStoredProduct();
      TrackingNumber trackingNumber =
          storedProducts.getTrackingNumber() != null ? storedProducts.getTrackingNumber() : null;
      BigDecimal qty = storedProducts.getStoredQty() != null ? storedProducts.getStoredQty() : null;
      Unit unit = storedProducts.getUnit() != null ? storedProducts.getUnit() : null;
      StockMove stockMove =
          stockMoveService.createStockMove(
              fromAddress,
              toAddress,
              company,
              fromStockLocation,
              toStockLocation,
              LocalDate.now(),
              LocalDate.now(),
              null,
              StockMoveRepository.TYPE_INTERNAL);
      StockMoveLine stockMoveLine =
          stockMoveLineService.createStockMoveLine(
              stockMove,
              product,
              trackingNumber,
              qty,
              qty,
              unit,
              null,
              fromStockLocation,
              toStockLocation);
      stockMove.setMassStockMove(storedProducts.getMassStockMove());
      stockMoveService.plan(stockMove);
      stockMoveService.realize(stockMove);
      stockMoveRepository.save(stockMove);
      stockMoveLineRepository.save(stockMoveLine);
      storedProducts.setStockMoveLine(stockMoveLine);
      storeProductsRepository.save(storedProducts);
    } else {
      if (storedProducts.getStoredQty().compareTo(BigDecimal.ZERO) == 0
          && storedProducts.getTrackingNumber() != null) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.STORED_QUANTITY_IS_ZERO)
                + " "
                + storedProducts.getStoredProduct().getFullName()
                + " "
                + storedProducts.getTrackingNumber().getTrackingNumberSeq());
      } else if (storedProducts.getStoredQty().compareTo(BigDecimal.ZERO) == 0
          && storedProducts.getTrackingNumber() == null) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.STORED_QUANTITY_IS_ZERO)
                + " "
                + storedProducts.getStoredProduct().getFullName());
      }
      if (storedProducts.getStockMoveLine() != null && storedProducts.getTrackingNumber() != null) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.ALREADY_STORED_PRODUCT)
                + " "
                + storedProducts.getStoredProduct()
                + " "
                + storedProducts.getTrackingNumber().getTrackingNumberSeq());
      } else if (storedProducts.getStockMoveLine() != null
          && storedProducts.getTrackingNumber() == null) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.ALREADY_STORED_PRODUCT)
                + " "
                + storedProducts.getStoredProduct());
      }
    }

    if (checkIfAllStoredProductAreStored(storedProducts.getMassStockMove())) {
      storedProducts.getMassStockMove().setStatusSelect(3);
      massStockMoveRepository.save(storedProducts.getMassStockMove());
    }
  }

  public boolean checkIfAllStoredProductAreStored(MassStockMove massStockMove) {
    boolean allIsStored = true;
    for (StoredProducts storedProducts : massStockMove.getStoredProductsList()) {
      if (!(storedProducts.getStoredQty().compareTo(storedProducts.getCurrentQty()) == 0
          && storedProducts.getStockMoveLine() != null)) {
        allIsStored = false;
        break;
      }
    }
    return allIsStored;
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
