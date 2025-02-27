package com.axelor.apps.businessproduction.observer;

import com.axelor.apps.businessproduction.service.SolDetailsBusinessProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.event.SaleOrderConfirm;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;

public class SaleOrderBusinessProdObserver {

  public void businessProdConfirmSaleOrder(@Observes SaleOrderConfirm event) {
    SaleOrder saleOrder = event.getSaleOrder();
    Beans.get(SolDetailsBusinessProductionService.class).copySolDetailsList(saleOrder);
  }
}
