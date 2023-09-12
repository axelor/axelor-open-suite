package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.MassStockMoveRepository;
import com.axelor.apps.stock.db.repo.PickedProductRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.StoredProductRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PickedProductServiceImpl implements PickedProductService {
  protected PickedProductRepository pickedProductRepository;
  protected StoredProductRepository storedProductRepository;
  protected MassStockMoveRepository massStockMoveRepository;
  protected StockMoveRepository stockMoveRepository;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected StockLocationLineRepository stockLocationLineRepository;
  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;

  @Inject
  public PickedProductServiceImpl(
      PickedProductRepository pickedProductRepository,
      StoredProductRepository storedProductRepository,
      MassStockMoveRepository massStockMoveRepository,
      StockMoveRepository stockMoveRepository,
      StockMoveLineRepository stockMoveLineRepository,
      StockLocationLineRepository stockLocationLineRepository,
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService) {
    this.pickedProductRepository = pickedProductRepository;
    this.storedProductRepository = storedProductRepository;
    this.massStockMoveRepository = massStockMoveRepository;
    this.stockMoveRepository = stockMoveRepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
  }

  @Override
  public PickedProduct createPickedProduct(
      MassStockMove massStockMove,
      Product product,
      TrackingNumber trackingNumber,
      StockLocation fromStockLocation,
      BigDecimal currentQty,
      BigDecimal pickedQty,
      StockMoveLine stockMoveLine) {

    PickedProduct pickedProduct = new PickedProduct();
    pickedProduct.setPickedProduct(product);
    pickedProduct.setTrackingNumber(trackingNumber);
    pickedProduct.setFromStockLocation(fromStockLocation);
    pickedProduct.setCurrentQty(currentQty);
    pickedProduct.setUnit(product.getUnit());
    pickedProduct.setPickedQty(pickedQty);
    pickedProduct.setStockMoveLine(stockMoveLine);
    pickedProduct.setMassStockMove(massStockMove);
    return pickedProduct;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createStockMoveForPickedProduct(
      PickedProduct pickedProduct, MassStockMove massStockMove) throws AxelorException {
    StockLocation fromStockLocation = pickedProduct.getFromStockLocation();
    StockLocation toStockLocation = massStockMove.getCartStockLocation();
    StockMove stockMove =
        stockMoveService.createStockMove(
            pickedProduct.getFromStockLocation().getAddress(),
            massStockMove.getCartStockLocation().getAddress(),
            massStockMove.getCompany(),
            fromStockLocation,
            toStockLocation,
            LocalDate.now(),
            LocalDate.now(),
            null,
            StockMoveRepository.TYPE_INTERNAL);

    stockMove.setMassStockMove(massStockMove);

    StockMoveLine stockMoveLine =
        stockMoveLineService.createStockMoveLine(
            stockMove,
            pickedProduct.getPickedProduct(),
            pickedProduct.getTrackingNumber(),
            pickedProduct.getPickedQty(),
            pickedProduct.getPickedQty(),
            pickedProduct.getUnit(),
            StockMoveLineRepository.CONFORMITY_NONE,
            fromStockLocation,
            toStockLocation);

    stockMoveService.plan(stockMove);
    stockMoveService.realize(stockMove);
    pickedProduct.setStockMoveLine(stockMoveLine);
    pickedProductRepository.save(pickedProduct);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createStoredProductFromPickedProduct(
      PickedProduct pickedProduct, MassStockMove massStockMove) throws AxelorException {
    String trackingNumberSeq =
        pickedProduct.getTrackingNumber() != null
            ? pickedProduct.getTrackingNumber().getTrackingNumberSeq()
            : "";
    StoredProduct storedProduct = new StoredProduct();
    storedProduct.setStoredProduct(pickedProduct.getPickedProduct());

    storedProduct.setTrackingNumber(pickedProduct.getTrackingNumber());
    storedProduct.setCurrentQty(pickedProduct.getPickedQty());
    storedProduct.setUnit(pickedProduct.getUnit());
    if (massStockMove.getCommonToStockLocation() != null) {
      storedProduct.setToStockLocation(massStockMove.getCommonToStockLocation());
    }
    storedProduct.setStoredQty(BigDecimal.ZERO);
    if (storedProduct.getStoredProduct() == null || storedProduct.getUnit() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_EMPTY_FIELD),
          pickedProduct.getPickedProduct().getFullName(),
          trackingNumberSeq);
    }
    if (storedProduct.getStoredProduct().getTrackingNumberConfiguration() != null
        && storedProduct.getTrackingNumber() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_EMPTY_TRACKING_NUMBER),
          pickedProduct.getPickedProduct().getFullName(),
          trackingNumberSeq);
    }
    if (storedProduct.getStoredProduct().getTrackingNumberConfiguration() == null
        && storedProduct.getUnit() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.MASS_STOCK_MOVE_EMPTY_FIELD_WITHOUT_TN),
          pickedProduct.getPickedProduct().getFullName(),
          trackingNumberSeq);
    }
    storedProductRepository.save(storedProduct);
    massStockMove.addStoredProductListItem(storedProduct);

    if (massStockMove.getStatusSelect() != MassStockMoveRepository.STATUS_IN_PROGRESS) {
      massStockMove.setStatusSelect(MassStockMoveRepository.STATUS_IN_PROGRESS);
    }

    massStockMoveRepository.save(massStockMove);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createStockMoveAndStockMoveLine(
      MassStockMove massStockMove, PickedProduct pickedProduct) throws AxelorException {
    StockLocation fromStockLocation = pickedProduct.getFromStockLocation();
    String trackingNumberSeq =
        pickedProduct.getTrackingNumber() != null
            ? pickedProduct.getTrackingNumber().getTrackingNumberSeq()
            : "";

    Query<StockLocationLine> query =
        stockLocationLineRepository
            .all()
            .filter(
                "self.stockLocation =?1 AND self.product =?2 AND self.currentQty =?3"
                            + pickedProduct.getTrackingNumber()
                        != null
                    ? " AND self.trackingNumber =?4"
                    : " AND self.detailsStockLocation = null",
                fromStockLocation,
                pickedProduct.getPickedProduct(),
                BigDecimal.ZERO,
                pickedProduct.getTrackingNumber());
    System.out.println("SALUT : " + query);
    StockLocationLine stockLocationLine =
        stockLocationLineRepository
            .all()
            .filter(
                "self.stockLocation =?1 AND self.product =?2 AND self.currentQty =?3"
                    + (pickedProduct.getTrackingNumber() != null
                        ? " AND self.trackingNumber =?4"
                        : " AND self.detailsStockLocation = null"),
                fromStockLocation,
                pickedProduct.getPickedProduct(),
                BigDecimal.ZERO,
                pickedProduct.getTrackingNumber())
            .fetchOne();

    if (stockLocationLine != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.PRODUCT_NO_AVAILABLE_IN_STOCKLOCATION_SOURCE),
          pickedProduct.getPickedProduct().getFullName(),
          trackingNumberSeq);
    }

    if (pickedProduct.getPickedQty().compareTo(BigDecimal.ZERO) != 0
        && pickedProduct.getPickedQty().compareTo(pickedProduct.getCurrentQty()) <= 0) {
      if (pickedProduct.getStockMoveLine() != null
          && pickedProduct.getStockMoveLine().getStockMove() != null
          && pickedProduct.getStockMoveLine().getStockMove().getStatusSelect()
              == StockMoveRepository.STATUS_REALIZED) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(StockExceptionMessage.ALREADY_PICKED_PRODUCT),
            pickedProduct.getPickedProduct().getFullName(),
            trackingNumberSeq);
      }
      createStockMoveForPickedProduct(pickedProduct, massStockMove);
      createStoredProductFromPickedProduct(pickedProduct, massStockMove);
    } else if (pickedProduct.getPickedQty().compareTo(BigDecimal.ZERO) != 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.PICKED_QUANTITY_GREATER_THAN_CURRENT_QTY),
          pickedProduct.getPickedProduct().getFullName(),
          trackingNumberSeq);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.PICKED_QUANTITY_IS_ZERO),
          pickedProduct.getPickedProduct().getFullName(),
          trackingNumberSeq);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void cancelStockMoveAndStockMoveLine(
      MassStockMove massStockMove, PickedProduct pickedProduct) throws AxelorException {
    if (pickedProduct.getStockMoveLine() != null) {
      StoredProduct storedProduct =
          storedProductRepository
              .all()
              .filter(
                  "self.storedProduct =?1 AND self.currentQty =?2 AND self.massStockMove =?3"
                      + (pickedProduct.getTrackingNumber() != null
                          ? " AND self.trackingNumber =?4"
                          : ""),
                  pickedProduct.getPickedProduct(),
                  pickedProduct.getPickedQty(),
                  massStockMove,
                  pickedProduct.getTrackingNumber())
              .fetchOne();
      storedProduct.setMassStockMove(null);
      storedProductRepository.remove(storedProduct);
      StockMove stockMove = pickedProduct.getStockMoveLine().getStockMove();
      if (stockMove != null) {
        stockMoveService.cancel(stockMove);
      }
      pickedProduct.setStockMoveLine(null);
      pickedProduct.setPickedQty(BigDecimal.ZERO);
      massStockMoveRepository.save(massStockMove);
      pickedProductRepository.save(pickedProduct);
    }
  }
}
