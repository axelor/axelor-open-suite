package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderCheckSaveService {

  void checkSaleOrderLineSubList(SaleOrder saleOrder) throws AxelorException;
}
