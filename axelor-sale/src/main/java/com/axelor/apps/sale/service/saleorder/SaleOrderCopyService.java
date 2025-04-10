package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderCopyService {

  void copySaleOrder(SaleOrder copy);

  void copySaleOrderProcess(SaleOrder copy);
}
