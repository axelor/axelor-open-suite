package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineCheckService {
  void productOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  void qtyOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;

  void unitOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;
}
