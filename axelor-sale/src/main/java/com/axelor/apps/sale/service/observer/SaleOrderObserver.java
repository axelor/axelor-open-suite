package com.axelor.apps.sale.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.event.SaleOrderConfirm;
import com.axelor.apps.sale.service.event.SaleOrderViewDummy;
import com.axelor.apps.sale.service.saleorder.SaleOrderConfirmService;
import com.axelor.apps.sale.service.saleorder.SaleOrderDummyService;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import java.util.Map;

public class SaleOrderObserver {

  public void saleConfirmSaleOrder(@Observes SaleOrderConfirm event) throws AxelorException {
    SaleOrder saleOrder = event.getSaleOrder();
    Beans.get(SaleOrderConfirmService.class).confirmProcess(saleOrder);
  }

  public void saleComputeDummiesSaleOrder(@Observes SaleOrderViewDummy event)
      throws AxelorException {
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Object> saleOrderMap = event.getSaleOrderMap();
    saleOrderMap.putAll(Beans.get(SaleOrderDummyService.class).getDummies(saleOrder));
  }
}
