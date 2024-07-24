package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderConfirmService {
  void confirmSaleOrder(SaleOrder saleOrder) throws AxelorException;
}
