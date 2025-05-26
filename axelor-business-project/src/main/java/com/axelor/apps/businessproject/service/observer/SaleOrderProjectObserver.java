package com.axelor.apps.businessproject.service.observer;

import com.axelor.apps.businessproject.service.SaleOrderCopyProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.event.SaleOrderCopy;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;

public class SaleOrderProjectObserver {

  public void copySaleOrder(@Observes SaleOrderCopy event) {
    SaleOrder saleOrder = event.getSaleOrder();
    Beans.get(SaleOrderCopyProjectService.class).copySaleOrderProjectProcess(saleOrder);
  }
}
