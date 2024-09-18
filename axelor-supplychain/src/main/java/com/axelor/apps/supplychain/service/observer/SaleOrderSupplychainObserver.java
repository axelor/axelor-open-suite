package com.axelor.apps.supplychain.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.event.SaleOrderConfirm;
import com.axelor.apps.supplychain.service.saleorder.status.SaleOrderConfirmSupplychainService;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;

public class SaleOrderSupplychainObserver {

  public void supplychainConfirmSaleOrder(@Observes SaleOrderConfirm event) throws AxelorException {
    SaleOrder saleOrder = event.getSaleOrder();
    event.setNotifyMessage(
        Beans.get(SaleOrderConfirmSupplychainService.class).confirmProcess(saleOrder));
  }
}
