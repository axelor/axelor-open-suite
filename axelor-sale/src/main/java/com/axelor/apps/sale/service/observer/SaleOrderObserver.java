package com.axelor.apps.sale.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.event.SaleOrderConfirm;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderConfirmService;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;

public class SaleOrderObserver {

  public void saleConfirmSaleOrder(@Observes SaleOrderConfirm event) throws AxelorException {
    SaleOrder saleOrder = event.getSaleOrder();
    Beans.get(SaleOrderConfirmService.class).confirmProcess(saleOrder);
  }
}
