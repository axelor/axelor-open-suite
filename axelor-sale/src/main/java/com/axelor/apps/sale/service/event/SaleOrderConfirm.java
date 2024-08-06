package com.axelor.apps.sale.service.event;

import com.axelor.apps.sale.db.SaleOrder;

public class SaleOrderConfirm {
  private final SaleOrder saleOrder;
  private String notifyMessage;

  public SaleOrderConfirm(SaleOrder saleOrder) {
    this.saleOrder = saleOrder;
  }

  public SaleOrder getSaleOrder() {
    return saleOrder;
  }

  public String getNotifyMessage() {
    return notifyMessage;
  }

  public void setNotifyMessage(String notifyMessage) {
    this.notifyMessage = notifyMessage;
  }
}
