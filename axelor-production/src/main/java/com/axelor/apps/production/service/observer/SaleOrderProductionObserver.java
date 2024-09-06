package com.axelor.apps.production.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.service.SaleOrderConfirmProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.event.SaleOrderConfirm;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;

public class SaleOrderProductionObserver {

  public void productionConfirmSaleOrder(@Observes SaleOrderConfirm event) throws AxelorException {
    SaleOrder saleOrder = event.getSaleOrder();
    Beans.get(SaleOrderConfirmProductionService.class).confirmProcess(saleOrder);
  }
}
