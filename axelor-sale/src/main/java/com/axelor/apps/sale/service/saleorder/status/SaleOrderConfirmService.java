package com.axelor.apps.sale.service.saleorder.status;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderConfirmService {
  String confirmSaleOrder(SaleOrder saleOrder);

  void confirmProcess(SaleOrder saleOrder) throws AxelorException;
}
