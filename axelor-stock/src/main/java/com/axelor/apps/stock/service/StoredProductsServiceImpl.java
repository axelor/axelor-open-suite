package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.MassMove;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.MassMoveRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.StoredProductsRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class StoredProductsServiceImpl implements StoredProductsService {

  protected StoredProductsRepository storeProductsRepository;
  protected MassMoveRepository massMoveRepository;

  @Inject
  public StoredProductsServiceImpl(
      StoredProductsRepository storeProductsRepository, MassMoveRepository massMoveRepository) {
    this.storeProductsRepository = storeProductsRepository;
    this.massMoveRepository = massMoveRepository;
  }

  @Override
  public StoredProducts createStoredProduct(
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal currentQty,
      StockLocation toStockLocation,
      BigDecimal storedQty,
      StockMoveLine stockMoveLine,
      MassMove massMove) {
    StoredProducts storedProducts = new StoredProducts();
    storedProducts.setStoredProduct(product);
    storedProducts.setTrackingNumber(trackingNumber);
    storedProducts.setCurrentQty(currentQty);
    storedProducts.setUnit(product.getUnit());
    storedProducts.setToStockLocation(toStockLocation);
    storedProducts.setStoredQty(storedQty);
    storedProducts.setStockMoveLine(stockMoveLine);
    storedProducts.setMassMove(massMove);
    return storedProducts;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public StoredProducts updateStoreProductMobility(
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal currentQty,
      Long storedProductId,
      StockLocation toStockLocation,
      BigDecimal storedQty,
      StockMoveLine stockMoveLine,
      MassMove massMove) {
    StoredProducts storedProducts = storeProductsRepository.find(storedProductId);
    storedProducts.setStoredProduct(product);
    storedProducts.setTrackingNumber(trackingNumber);
    storedProducts.setCurrentQty(currentQty);
    storedProducts.setUnit(product.getUnit());
    storedProducts.setToStockLocation(toStockLocation);
    storedProducts.setStoredQty(storedQty);
    storedProducts.setStockMoveLine(stockMoveLine);
    storedProducts.setMassMove(massMove);
    storeProductsRepository.save(storedProducts);
    return storedProducts;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public StoredProducts createStoredProductMobility(
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal currentQty,
      StockLocation toStockLocation,
      BigDecimal storedQty,
      StockMoveLine stockMoveLine,
      MassMove massMove) {
    StoredProducts storedProducts =
        createStoredProduct(
            product,
            trackingNumber,
            currentQty,
            toStockLocation,
            storedQty,
            stockMoveLine,
            massMove);
    storeProductsRepository.save(storedProducts);
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
      Product duplicateProduct =
          storedProducts.getStoredProduct() != null ? storedProducts.getStoredProduct() : null;
      TrackingNumber duplicateTrackingNumber =
          storedProducts.getTrackingNumber() != null ? storedProducts.getTrackingNumber() : null;
      StockLocation duplicateToStockLocation =
          storedProducts.getToStockLocation() != null ? storedProducts.getToStockLocation() : null;
      StockMoveLine duplicateStockMoveLine =
          storedProducts.getStockMoveLine() != null ? storedProducts.getStockMoveLine() : null;
      MassMove duplicateMassMove =
          storedProducts.getMassMove() != null ? storedProducts.getMassMove() : null;
      storedProducts.setCurrentQty(storedQty);
      StoredProducts duplicateStoredProduct =
          createStoredProduct(
              duplicateProduct,
              duplicateTrackingNumber,
              currentQty.subtract(storedQty),
              duplicateToStockLocation,
              BigDecimal.ZERO,
              duplicateStockMoveLine,
              duplicateMassMove);
      storeProductsRepository.save(duplicateStoredProduct);
    }

    if (storedProducts.getStoredQty().compareTo(BigDecimal.ZERO) != 0
        && storedProducts.getToStockLocation() != null
        && storedProducts.getStockMoveLine() == null) {

      if (storedProducts.getStoredQty().compareTo(storedProducts.getCurrentQty()) == 1) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.STORED_QUANTITY_GREATER_THAN_CURRENT_QTY)
                + " "
                + storedProducts.getStoredProduct().getFullName()
                + " "
                + storedProducts.getTrackingNumber().getTrackingNumberSeq());
      }
      Address toAddress = storedProducts.getToStockLocation().getAddress();
      Address fromAddress = storedProducts.getMassMove().getCartStockLocation().getAddress();
      Company company = storedProducts.getMassMove().getCompany();
      StockLocation toStockLocation = storedProducts.getToStockLocation();
      StockLocation fromStockLocation = storedProducts.getMassMove().getCartStockLocation();
      Product product = storedProducts.getStoredProduct();
      TrackingNumber trackingNumber =
          storedProducts.getTrackingNumber() != null ? storedProducts.getTrackingNumber() : null;
      BigDecimal qty = storedProducts.getStoredQty() != null ? storedProducts.getStoredQty() : null;
      Unit unit = storedProducts.getUnit() != null ? storedProducts.getUnit() : null;
      StockMove stockMove =
          Beans.get(StockMoveService.class)
              .createStockMove(
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
          Beans.get(StockMoveLineService.class)
              .createStockMoveLine(
                  stockMove,
                  product,
                  trackingNumber,
                  qty,
                  qty,
                  unit,
                  null,
                  fromStockLocation,
                  toStockLocation);
      stockMove.setMassMove(storedProducts.getMassMove());
      Beans.get(StockMoveService.class).plan(stockMove);
      Beans.get(StockMoveService.class).realize(stockMove);
      Beans.get(StockMoveRepository.class).save(stockMove);
      Beans.get(StockMoveLineRepository.class).save(stockMoveLine);
      storedProducts.setStockMoveLine(stockMoveLine);
      storeProductsRepository.save(storedProducts);
    } else {
      if (storedProducts.getStoredQty().compareTo(BigDecimal.ZERO) == 0) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.STORED_QUANTITY_IS_ZERO)
                + " "
                + storedProducts.getStoredProduct().getFullName()
                + " "
                + storedProducts.getTrackingNumber().getTrackingNumberSeq());
      }
      if (storedProducts.getStockMoveLine() != null) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.ALREADY_STORED_PRODUCT)
                + " "
                + storedProducts.getStoredProduct()
                + " "
                + storedProducts.getTrackingNumber().getTrackingNumberSeq());
      }
    }

    if (checkIfAllStoredProductAreStored(storedProducts.getMassMove())) {
      storedProducts.getMassMove().setStatusSelect(3);
      massMoveRepository.save(storedProducts.getMassMove());
    }
  }

  public boolean checkIfAllStoredProductAreStored(MassMove massMove) {
    boolean allIsStored = true;
    for (StoredProducts storedProducts : massMove.getStoredProductsList()) {
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
      Beans.get(StockMoveService.class).cancel(storedProducts.getStockMoveLine().getStockMove());
      storedProducts.setStockMoveLine(null);
      storedProducts.setStoredQty(BigDecimal.ZERO);
      storeProductsRepository.save(storedProducts);
    }
  }
}
