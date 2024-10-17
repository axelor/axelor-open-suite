package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class MassStockMovableProductRealizeServiceImpl
    implements MassStockMovableProductRealizeService {

  protected final StockMoveService stockMoveService;
  protected final AppBaseService appBaseService;
  protected final StockMoveLineService stockMoveLineService;
  protected final StockLocationLineRepository stockLocationLineRepository;
  protected final MassStockMovableProductServiceFactory massStockMovableProductServiceFactory;
  protected final MassStockMovableProductQuantityService massStockMovableProductQuantityService;

  @Inject
  public MassStockMovableProductRealizeServiceImpl(
      StockMoveService stockMoveService,
      AppBaseService appBaseService,
      StockMoveLineService stockMoveLineService,
      StockLocationLineRepository stockLocationLineRepository,
      MassStockMovableProductServiceFactory massStockMovableProductServiceFactory,
      MassStockMovableProductQuantityService massStockMovableProductQuantityService) {
    this.stockMoveService = stockMoveService;
    this.appBaseService = appBaseService;
    this.stockMoveLineService = stockMoveLineService;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.massStockMovableProductServiceFactory = massStockMovableProductServiceFactory;
    this.massStockMovableProductQuantityService = massStockMovableProductQuantityService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void realize(List<? extends MassStockMovableProduct> massStockMovableProducts)
      throws AxelorException {
    Objects.requireNonNull(massStockMovableProducts);

    if (!massStockMovableProducts.isEmpty()) {
      MassStockMovableProductProcessingRealizeService processingService =
          massStockMovableProductServiceFactory.getMassStockMovableProductProcessingRealizeService(
              massStockMovableProducts.get(0));
      MassStockMovableProductLocationService locationService =
          massStockMovableProductServiceFactory.getMassStockMovableProductLocationService(
              massStockMovableProducts.get(0));
      MassStockMovableProductProcessingSaveService saveService =
          massStockMovableProductServiceFactory.getMassStockMovableProductProcessingSaveService(
              massStockMovableProducts.get(0));

      for (MassStockMovableProduct massStockMovableProduct : massStockMovableProducts) {
        realize(massStockMovableProduct, processingService, saveService, locationService);
      }
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void realize(
      MassStockMovableProduct movableProduct,
      MassStockMovableProductProcessingRealizeService processingService,
      MassStockMovableProductProcessingSaveService saveService,
      MassStockMovableProductLocationService locationService)
      throws AxelorException {

    var massStockMove = movableProduct.getMassStockMove();
    var fromStockLocation = locationService.getFromStockLocation(movableProduct);
    var toStockLocation = locationService.getToStockLocation(movableProduct);
    var todayDate = appBaseService.getTodayDate(massStockMove.getCompany());

    if (movableProduct.getStockMoveLine() == null) {
      checkStockLocations(movableProduct, toStockLocation, fromStockLocation);
      checkQty(movableProduct, fromStockLocation);

      processingService.preRealize(movableProduct);

      var stockMove =
          stockMoveService.createStockMove(
              fromStockLocation.getAddress(),
              toStockLocation.getAddress(),
              massStockMove.getCompany(),
              fromStockLocation,
              toStockLocation,
              todayDate,
              todayDate,
              StockMoveRepository.TYPE_INTERNAL,
              massStockMove);

      var stockMoveLine =
          stockMoveLineService.createStockMoveLine(
              stockMove,
              movableProduct.getProduct(),
              movableProduct.getTrackingNumber(),
              movableProduct.getMovedQty(),
              movableProduct.getMovedQty(),
              movableProduct.getUnit(),
              StockMoveLineRepository.CONFORMITY_NONE,
              fromStockLocation,
              toStockLocation);

      stockMoveService.plan(stockMove);
      stockMoveService.realize(stockMove);
      movableProduct.setStockMoveLine(stockMoveLine);

      processingService.postRealize(movableProduct);

      saveService.save(movableProduct);
    }
  }

  protected void checkStockLocations(
      MassStockMovableProduct movableProduct,
      StockLocation toStockLocation,
      StockLocation fromStockLocation)
      throws AxelorException {
    if (toStockLocation == null || fromStockLocation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              StockExceptionMessage.STOCK_MOVE_MASS_PRODUCT_NO_STOCK_LOCATION_SOURCE_AVAILABLE),
          movableProduct.getProduct().getFullName());
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void realize(MassStockMovableProduct movableProduct) throws AxelorException {
    Objects.requireNonNull(movableProduct);

    MassStockMovableProductProcessingRealizeService processingService =
        massStockMovableProductServiceFactory.getMassStockMovableProductProcessingRealizeService(
            movableProduct);
    MassStockMovableProductLocationService locationService =
        massStockMovableProductServiceFactory.getMassStockMovableProductLocationService(
            movableProduct);
    MassStockMovableProductProcessingSaveService saveService =
        massStockMovableProductServiceFactory.getMassStockMovableProductProcessingSaveService(
            movableProduct);

    realize(movableProduct, processingService, saveService, locationService);
  }

  protected void checkQty(MassStockMovableProduct movableProduct, StockLocation fromStockLocation)
      throws AxelorException {
    var stockLocationLine =
        stockLocationLineRepository
            .all()
            .filter(
                "self.stockLocation = :fromStockLocation AND self.product = :product AND self.currentQty = :qty"
                    + (movableProduct.getTrackingNumber() != null
                        ? " AND self.trackingNumber = :trackingNumber"
                        : " AND self.detailsStockLocation = null"))
            .bind("fromStockLocation", fromStockLocation)
            .bind("product", movableProduct.getProduct())
            .bind("qty", BigDecimal.ZERO)
            .bind("trackingNumber", movableProduct.getTrackingNumber())
            .fetchOne();

    if (stockLocationLine != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              StockExceptionMessage.STOCK_MOVE_MASS_PRODUCT_NO_AVAILABLE_IN_STOCKLOCATION_SOURCE),
          movableProduct.getProduct().getFullName());
    }

    var availableQty =
        massStockMovableProductQuantityService.getCurrentAvailableQty(
            movableProduct, fromStockLocation);

    if (movableProduct.getMovedQty().compareTo(availableQty) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              StockExceptionMessage.STOCK_MOVE_MASS_PRODUCT_NO_AVAILABLE_IN_STOCKLOCATION_SOURCE),
          movableProduct.getProduct().getFullName());
    }

    if (movableProduct.getMovedQty().compareTo(BigDecimal.ZERO) <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.STOCK_MOVE_MASS_MOVED_QUANTITY_IS_ZERO_OR_LESS),
          movableProduct.getProduct().getFullName());
    }
  }
}
