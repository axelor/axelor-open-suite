package com.axelor.apps.production.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.service.SaleOrderLineProductProductionService;
import com.axelor.apps.production.service.SaleOrderLineViewProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnLoad;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnNew;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import java.util.Map;

public class SaleOrderLineProductionObserver {
  void onSaleOrderLineOnNew(@Observes SaleOrderLineViewOnNew event) {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    SaleOrderLineViewProductionService saleOrderLineViewProductionService =
        Beans.get(SaleOrderLineViewProductionService.class);
    saleOrderLineMap.putAll(
        saleOrderLineViewProductionService.hideBomAndProdProcess(saleOrderLine));
    saleOrderLineMap.putAll(saleOrderLineViewProductionService.getSolDetailsScale());
  }

  void onSaleOrderLineOnLoad(@Observes SaleOrderLineViewOnLoad event) {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    SaleOrderLineViewProductionService saleOrderLineViewProductionService =
        Beans.get(SaleOrderLineViewProductionService.class);
    saleOrderLineMap.putAll(
        saleOrderLineViewProductionService.hideBomAndProdProcess(saleOrderLine));
    saleOrderLineMap.putAll(saleOrderLineViewProductionService.getSolDetailsScale());
  }

  void onSaleOrderLineProductOnChange(@Observes SaleOrderLineProductOnChange event)
      throws AxelorException {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Object> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineProductProductionService.class)
            .computeProductInformationProduction(saleOrderLine, saleOrder));
  }
}
