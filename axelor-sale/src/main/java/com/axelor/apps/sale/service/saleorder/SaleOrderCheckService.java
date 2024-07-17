package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderCheckService {
  String finalizeCheckAlert(SaleOrder saleOrder) throws AxelorException;

  void checkSaleOrderLineList(SaleOrder saleOrder) throws AxelorException;

  boolean productSoldAtLoss(SaleOrder saleOrder);

  boolean priceListIsNotValid(SaleOrder saleOrder);
}
