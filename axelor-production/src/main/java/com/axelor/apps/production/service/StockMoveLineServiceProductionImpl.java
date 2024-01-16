package com.axelor.apps.production.service;

import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ShippingCoefService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.service.StockLocationLineHistoryService;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.service.ProductReservationService;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import java.math.BigDecimal;

public class StockMoveLineServiceProductionImpl extends StockMoveLineServiceSupplychainImpl {
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
      ShippingCoefService shippingCoefService,
      AccountManagementService accountManagementService,
      PriceListService priceListService,
      ProductCompanyService productCompanyService,
      SupplychainBatchRepository supplychainBatchRepo,
      SupplyChainConfigService supplychainConfigService,
      StockLocationLineHistoryService stockLocationLineHistoryService,
      InvoiceLineRepository invoiceLineRepository,
      ProductReservationRepository productReservationRepository,
      ProductReservationService productReservationService,
      AppSupplychainService appSupplychainService) {

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
        shippingCoefService,
        accountManagementService,
        priceListService,
        productCompanyService,
        supplychainBatchRepo,
        supplychainConfigService,
        stockLocationLineHistoryService,
        invoiceLineRepository,
        productReservationRepository,
        productReservationService,
        appSupplychainService);
  }

  @Override
  public boolean isAllocationToBeSelected(StockMoveLine stockMoveLine) {
    return stockMoveLine
            .getRealQty()
            .compareTo(
                stockMoveLine.getProductReservationList().stream()
                    .map(it -> it.getQty())
                    .reduce(BigDecimal.ZERO, BigDecimal::add))
        != 0;
  }
}
