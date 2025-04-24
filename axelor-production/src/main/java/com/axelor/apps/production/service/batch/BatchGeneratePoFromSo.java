package com.axelor.apps.production.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderSaleOrderService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppProduction;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class BatchGeneratePoFromSo extends BatchStrategy {

  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final ManufOrderSaleOrderService manufOrderSaleOrderService;
  protected final AppProductionService appProductionService;
  protected final ProductionOrderSaleOrderService productionOrderSaleOrderService;

  @Inject
  public BatchGeneratePoFromSo(
      SaleOrderLineRepository saleOrderLineRepository,
      ManufOrderSaleOrderService manufOrderSaleOrderService,
      AppProductionService appProductionService,
      ProductionOrderSaleOrderService productionOrderSaleOrderService) {
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.manufOrderSaleOrderService = manufOrderSaleOrderService;
    this.appProductionService = appProductionService;
    this.productionOrderSaleOrderService = productionOrderSaleOrderService;
  }

  @Override
  protected void process() {
    Query<SaleOrderLine> saleOrderLineQuery =
        saleOrderLineRepository
            .all()
            .filter(
                "self.saleOrder.statusSelect = :confirmedStatus AND "
                    + "self.qtyProduced < self.qtyToProduce AND "
                    + "(self.saleSupplySelect = :produceSupplySelect OR self.saleSupplySelect = :productOrStockSupplySelect)")
            .bind("confirmedStatus", SaleOrderRepository.STATUS_ORDER_CONFIRMED)
            .bind("produceSupplySelect", SaleOrderLineRepository.SALE_SUPPLY_PRODUCE)
            .bind(
                "productOrStockSupplySelect",
                SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK_AND_PRODUCE);

    List<SaleOrderLine> saleOrderLineList;
    int offset = 0;
    while (!(saleOrderLineList = saleOrderLineQuery.fetch(getFetchLimit(), offset)).isEmpty()) {
      try {
        for (SaleOrderLine saleOrderLine : saleOrderLineList) {
          offset++;
          saleOrderLine = saleOrderLineRepository.find(saleOrderLine.getId());
          if (!productionOrderSaleOrderService.isGenerationNeeded(saleOrderLine)) {
            continue;
          }
          generatePoFromSol(saleOrderLine);
          incrementDone();
        }
        JPA.clear();
        findBatch();
      } catch (Exception e) {
        TraceBackService.trace(e, "Production orders generation from sale orders", batch.getId());
        incrementAnomaly();
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void generatePoFromSol(SaleOrderLine saleOrderLine) throws AxelorException {
    SaleOrder saleOrder = saleOrderLine.getSaleOrder();
    AppProduction appProduction = appProductionService.getAppProduction();
    boolean onePoPerSo = appProduction.getOneProdOrderPerSO();
    if (onePoPerSo) {
      ProductionOrder productionOrder =
          productionOrderSaleOrderService.fetchOrCreateProductionOrder(saleOrder);
      if (manufOrderSaleOrderService
              .computeQuantityToProduceLeft(saleOrderLine)
              .compareTo(BigDecimal.ZERO)
          > 0) {
        manufOrderSaleOrderService.generateManufOrders(productionOrder, saleOrderLine);
      }
    } else {
      ProductionOrder productionOrder =
          productionOrderSaleOrderService.fetchOrCreateProductionOrder(saleOrder);
      manufOrderSaleOrderService.generateManufOrders(productionOrder, saleOrderLine);
    }
  }

  @Override
  protected void stop() {
    super.stop();
    addComment(
        String.format(
            I18n.get("%d line(s) treated and %d anomaly(ies) reported !"),
            batch.getDone(),
            batch.getAnomaly()));
  }
}
