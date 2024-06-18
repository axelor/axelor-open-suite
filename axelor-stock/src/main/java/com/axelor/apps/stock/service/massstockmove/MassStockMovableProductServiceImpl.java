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

public class MassStockMovableProductServiceImpl implements MassStockMovableProductService {

  protected final StockMoveService stockMoveService;
  protected final AppBaseService appBaseService;
  protected final StockMoveLineService stockMoveLineService;
  protected final StockLocationLineRepository stockLocationLineRepository;
  protected final MassStockMovableProductServiceFactory massStockMovableProductServiceFactory;

  @Inject
  public MassStockMovableProductServiceImpl(
      StockMoveService stockMoveService,
      AppBaseService appBaseService,
      StockMoveLineService stockMoveLineService,
      StockLocationLineRepository stockLocationLineRepository,
      MassStockMovableProductServiceFactory massStockMovableProductServiceFactory) {
    this.stockMoveService = stockMoveService;
    this.appBaseService = appBaseService;
    this.stockMoveLineService = stockMoveLineService;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.massStockMovableProductServiceFactory = massStockMovableProductServiceFactory;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void realize(List<? extends MassStockMovableProduct> massStockMovableProducts)
      throws AxelorException {
    Objects.requireNonNull(massStockMovableProducts);

    for (MassStockMovableProduct massStockMovableProduct : massStockMovableProducts) {
      realize(massStockMovableProduct);
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void realize(MassStockMovableProduct movableProduct) throws AxelorException {
    Objects.requireNonNull(movableProduct);

    var massStockMove = movableProduct.getMassStockMove();
    var fromStockLocation = movableProduct.getStockLocation();
    var toStockLocation = massStockMove.getCartStockLocation();
    var todayDate = appBaseService.getTodayDate(massStockMove.getCompany());

    if (movableProduct.getStockMoveLine() == null) {
      checkQty(movableProduct, fromStockLocation);

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

      MassStockMovableProductProcessingService processingService =
          massStockMovableProductServiceFactory.getMassStockMovableProductProcessingService(
              movableProduct);
      processingService.save(movableProduct);
    }
  }

  protected void checkQty(MassStockMovableProduct movableProduct, StockLocation fromStockLocation)
      throws AxelorException {
    var stockLocationLine =
        stockLocationLineRepository
            .all()
            .filter(
                "self.stockLocation =?1 AND self.product =?2 AND self.currentQty =?3"
                    + (movableProduct.getTrackingNumber() != null
                        ? " AND self.trackingNumber =?4"
                        : " AND self.detailsStockLocation = null"),
                fromStockLocation,
                movableProduct.getProduct(),
                BigDecimal.ZERO,
                movableProduct.getTrackingNumber())
            .fetchOne();

    if (stockLocationLine != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              StockExceptionMessage.STOCK_MOVE_MASS_PRODUCT_NO_AVAILABLE_IN_STOCKLOCATION_SOURCE),
          movableProduct.getProduct().getFullName());
    }

    if (movableProduct.getMovedQty().compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.STOCK_MOVE_MASS_MOVED_QUANTITY_IS_ZERO),
          movableProduct.getProduct().getFullName());
    }

    if (movableProduct.getMovedQty().compareTo(movableProduct.getCurrentQty()) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.STOCK_MOVE_MASS_MOVED_QTY_GREATER_THAN_CURRENT_QTY),
          movableProduct.getProduct().getFullName());
    }
  }
}
