package com.axelor.apps.supplychain.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnLoad;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnNew;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineViewSupplychainService;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import java.util.Map;

public class SaleOrderLineSupplychainObserver {
  void onSaleOrderLineOnNew(@Observes SaleOrderLineViewOnNew event) throws AxelorException {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineViewSupplychainService.class)
            .getSupplychainOnNewAttrs(saleOrderLine, saleOrder));
  }

  void onSaleOrderLineOnLoad(@Observes SaleOrderLineViewOnLoad event) throws AxelorException {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineViewSupplychainService.class)
            .getSupplychainOnLoadAttrs(saleOrderLine, saleOrder));
  }

  void onSaleOrderLineProductOnChange(@Observes SaleOrderLineProductOnChange event)
      throws AxelorException {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Object> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineProductSupplychainService.class)
            .computeProductInformationSupplychain(saleOrderLine, saleOrder));
  }
}
