package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.PickedProductsRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.StoredProductsRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PickedProductsServiceImpl implements PickedProductsService {
  protected PickedProductsRepository pickedProductsRepository;
  protected StoredProductsRepository storedProductsRepository;
  protected MassStockMoveRepository massStockMoveRepository;
  protected StockMoveRepository stockMoveRepository;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected StockLocationLineRepository stockLocationLineRepository;
  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;

  @Inject
  public PickedProductsServiceImpl(
      PickedProductsRepository pickedProductsRepository,
      StoredProductsRepository storedProductsRepository,
      MassStockMoveRepository massStockMoveRepository,
      StockMoveRepository stockMoveRepository,
      StockMoveLineRepository stockMoveLineRepository,
      StockLocationLineRepository stockLocationLineRepository,
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService) {
    this.pickedProductsRepository = pickedProductsRepository;
    this.storedProductsRepository = storedProductsRepository;
    this.massStockMoveRepository = massStockMoveRepository;
    this.stockMoveRepository = stockMoveRepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public PickedProducts createPickedProduct(
      MassStockMove massStockMove,
      Product pickedProduct,
      TrackingNumber trackingNumber,
      StockLocation fromStockLocation,
      BigDecimal currentQty,
      BigDecimal pickedQty,
      StockMoveLine stockMoveLine) {

    PickedProducts pickedProducts = new PickedProducts();
    pickedProducts.setPickedProduct(pickedProduct);
    pickedProducts.setTrackingNumber(trackingNumber);
    pickedProducts.setFromStockLocation(fromStockLocation);
    pickedProducts.setCurrentQty(currentQty);
    pickedProducts.setUnit(pickedProduct.getUnit());
    pickedProducts.setPickedQty(pickedQty);
    pickedProducts.setStockMoveLine(stockMoveLine);
    pickedProducts.setMassStockMove(massStockMove);
    pickedProductsRepository.save(pickedProducts);
    return pickedProducts;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createStockMoveAndStockMoveLine(
      MassStockMove massStockMove, PickedProducts pickedProducts) throws AxelorException {
    StockLocationLine stockLocationLine = null;
    StockLocation fromStockLocation = pickedProducts.getFromStockLocation();
    StockLocation toStockLocation = massStockMove.getCartStockLocation();
    String trackingNumberSeq =
        pickedProducts.getTrackingNumber() != null
            ? pickedProducts.getTrackingNumber().getTrackingNumberSeq()
            : null;
    if (pickedProducts.getTrackingNumber() != null) {
      stockLocationLine =
          stockLocationLineRepository
              .all()
              .filter(
                  "self.stockLocation =?1 AND self.product =?2 AND self.currentQty =?3 AND self.trackingNumber =?4",
                  fromStockLocation,
                  pickedProducts.getPickedProduct(),
                  BigDecimal.ZERO,
                  pickedProducts.getTrackingNumber())
              .fetchOne();
    } else {
      stockLocationLine =
          stockLocationLineRepository
              .all()
              .filter(
                  "self.stockLocation =?1 AND self.product =?2 AND self.currentQty =?3 AND self.detailsStockLocation = null",
                  fromStockLocation,
                  pickedProducts.getPickedProduct(),
                  BigDecimal.ZERO)
              .fetchOne();
    }

    if ((stockLocationLine != null)) {
      throw new AxelorException(
          0,
          I18n.get(StockExceptionMessage.PRODUCT_NO_AVAILABLE_IN_STOCKLOCATION_SOURCE)
              + " "
              + pickedProducts.getPickedProduct().getFullName()
              + " "
              + trackingNumberSeq);
    }

    if (pickedProducts.getPickedQty().compareTo(BigDecimal.ZERO) != 0
        && pickedProducts.getPickedQty().compareTo(pickedProducts.getCurrentQty()) <= 0) {
      if (pickedProducts.getStockMoveLine() != null
          && pickedProducts.getStockMoveLine().getStockMove() != null
          && pickedProducts.getStockMoveLine().getStockMove().getStatusSelect()
              == StockMoveRepository.STATUS_REALIZED) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.ALREADY_PICKED_PRODUCT)
                + " "
                + pickedProducts.getPickedProduct().getFullName()
                + " "
                + trackingNumberSeq);
      }
      Address fromAddress = pickedProducts.getFromStockLocation().getAddress();
      Address toAddress = massStockMove.getCartStockLocation().getAddress();
      Company company = massStockMove.getCompany() != null ? massStockMove.getCompany() : null;
      LocalDate toDayDate = LocalDate.now();
      StockMove stockMove =
          stockMoveService.createStockMove(
              fromAddress,
              toAddress,
              company,
              fromStockLocation,
              toStockLocation,
              toDayDate,
              toDayDate,
              null,
              StockMoveRepository.TYPE_INTERNAL);

      stockMove.setMassStockMove(massStockMove);

      StockMoveLine stockMoveLine =
          stockMoveLineService.createStockMoveLine(
              stockMove,
              pickedProducts.getPickedProduct(),
              pickedProducts.getTrackingNumber(),
              pickedProducts.getPickedQty(),
              pickedProducts.getPickedQty(),
              pickedProducts.getUnit(),
              StockMoveLineRepository.CONFORMITY_NONE,
              fromStockLocation,
              toStockLocation);

      stockMoveService.plan(stockMove);
      stockMoveService.realize(stockMove);
      stockMoveRepository.save(stockMove);
      stockMoveLineRepository.save(stockMoveLine);
      pickedProducts.setStockMoveLine(stockMoveLine);
      pickedProductsRepository.save(pickedProducts);
      StoredProducts storedProducts = new StoredProducts();
      storedProducts.setStoredProduct(pickedProducts.getPickedProduct());

      storedProducts.setTrackingNumber(pickedProducts.getTrackingNumber());
      storedProducts.setCurrentQty(pickedProducts.getPickedQty());
      storedProducts.setUnit(pickedProducts.getUnit());
      if (massStockMove.getCommonToStockLocation() != null) {
        storedProducts.setToStockLocation(massStockMove.getCommonToStockLocation());
      }
      storedProducts.setStoredQty(BigDecimal.ZERO);
      if (storedProducts.getStoredProduct() == null || storedProducts.getUnit() == null) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_EMPTY_FIELD)
                + " "
                + pickedProducts.getPickedProduct().getFullName()
                + " "
                + trackingNumberSeq);
      }
      if (storedProducts.getStoredProduct().getTrackingNumberConfiguration() != null
          && storedProducts.getTrackingNumber() == null) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_EMPTY_TRACKING_NUMBER)
                + " "
                + pickedProducts.getPickedProduct().getFullName()
                + " "
                + trackingNumberSeq);
      }
      if (storedProducts.getStoredProduct().getTrackingNumberConfiguration() == null
          && storedProducts.getUnit() == null) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_EMPTY_FIELD_WITHOUT_TN)
                + " "
                + pickedProducts.getPickedProduct().getFullName()
                + " "
                + trackingNumberSeq);
      }
      storedProductsRepository.save(storedProducts);
      massStockMove.addStoredProductsListItem(storedProducts);
      if (massStockMove.getStatusSelect() != 2) {
        massStockMove.setStatusSelect(2);
      }
      Beans.get(MassStockMoveRepository.class).save(massStockMove);
    } else {
      if (pickedProducts.getPickedQty().compareTo(BigDecimal.ZERO) != 0) {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY)
                + " "
                + pickedProducts.getPickedProduct().getFullName()
                + " "
                + trackingNumberSeq);
      } else {
        throw new AxelorException(
            0,
            I18n.get(StockExceptionMessage.PICKED_QUANTITY_IS_ZERO)
                + " "
                + pickedProducts.getPickedProduct().getFullName()
                + " "
                + trackingNumberSeq);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void cancelStockMoveAndStockMoveLine(
      MassStockMove massStockMove, PickedProducts pickedProducts) {

    try {
      if (pickedProducts.getStockMoveLine() != null) {
        StoredProducts storedProducts = null;
        if (pickedProducts.getTrackingNumber() != null) {
          storedProducts =
              storedProductsRepository
                  .all()
                  .filter(
                      "self.storedProduct =?1 AND self.trackingNumber =?2 AND self.currentQty =?3 AND self.massStockMove =?4",
                      pickedProducts.getPickedProduct(),
                      pickedProducts.getTrackingNumber(),
                      pickedProducts.getPickedQty(),
                      massStockMove)
                  .fetchOne();
        } else {
          storedProducts =
              storedProductsRepository
                  .all()
                  .filter(
                      "self.storedProduct =?1 AND self.currentQty =?2 AND self.massStockMove =?3",
                      pickedProducts.getPickedProduct(),
                      pickedProducts.getPickedQty(),
                      massStockMove)
                  .fetchOne();
        }
        storedProducts.setMassStockMove(null);
        storedProductsRepository.remove(storedProducts);
        StockMove stockMove = pickedProducts.getStockMoveLine().getStockMove();
        stockMoveService.cancel(stockMove);
        pickedProducts.setStockMoveLine(null);
        pickedProducts.setPickedQty(BigDecimal.ZERO);
        massStockMoveRepository.save(massStockMove);
        pickedProductsRepository.save(pickedProducts);
        JPA.flush();
      }
    } catch (AxelorException e) {
      e.printStackTrace();
    }
  }
}
