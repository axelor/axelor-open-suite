package com.axelor.apps.sale.service.observer;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.event.SaleOrderConfirm;
import com.axelor.event.Event;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderFireServiceImpl implements SaleOrderFireService {
  protected Event<SaleOrderConfirm> saleOrderConfirmEvent;

  @Inject
  public SaleOrderFireServiceImpl(Event<SaleOrderConfirm> saleOrderConfirmEvent) {
    this.saleOrderConfirmEvent = saleOrderConfirmEvent;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public String confirmSaleOrder(SaleOrder saleOrder) {
    SaleOrderConfirm saleOrderConfirm = new SaleOrderConfirm(saleOrder);
    saleOrderConfirmEvent.fire(saleOrderConfirm);
    return saleOrderConfirm.getNotifyMessage();
  }
}
