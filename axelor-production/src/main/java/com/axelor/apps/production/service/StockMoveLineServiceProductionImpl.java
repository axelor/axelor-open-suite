package com.axelor.apps.production.service;

import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychainImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class StockMoveLineServiceProductionImpl extends StockMoveLineServiceSupplychainImpl {

  @Inject
  public StockMoveLineServiceProductionImpl(
      TrackingNumberService trackingNumberService,
      AppBaseService appBaseService,
      AppStockService appStockService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      StockLocationLineService stockLocationLineService,
      UnitConversionService unitConversionService,
      WeightedAveragePriceService weightedAveragePriceService,
      TrackingNumberRepository trackingNumberRepo,
      AccountManagementService accountManagementService,
      PriceListService priceListService) {
    super(
        trackingNumberService,
        appBaseService,
        appStockService,
        stockMoveToolService,
        stockMoveLineRepository,
        stockLocationLineService,
        unitConversionService,
        weightedAveragePriceService,
        trackingNumberRepo,
        accountManagementService,
        priceListService);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected List<String> checkTrackingNumberAndGenerateErrors(StockMove stockMove)
      throws AxelorException {
    List<String> productsWithErrors = super.checkTrackingNumberAndGenerateErrors(stockMove);

    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      if (stockMoveLine.getProduct() == null) {
        continue;
      }

      TrackingNumberConfiguration trackingNumberConfig =
          stockMoveLine.getProduct().getTrackingNumberConfiguration();

      if (stockMoveLine.getProduct() != null
          && trackingNumberConfig != null
          && trackingNumberConfig.getIsProductionTrackingManaged()
          && (stockMove.getOriginTypeSelect() == StockMoveRepository.ORIGIN_OPERATION_ORDER
              || stockMove.getOriginTypeSelect() == StockMoveRepository.ORIGIN_MANUF_ORDER)
          && stockMoveLine.getTrackingNumber() == null
          && stockMoveLine.getRealQty().compareTo(BigDecimal.ZERO) != 0) {

        productsWithErrors.add(stockMoveLine.getProduct().getName());
      }
    }
    return productsWithErrors;
  }
}
