package com.axelor.apps.sale.service.saleorder.status;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderFinalizeService {
  void finalizeQuotation(SaleOrder saleOrder) throws AxelorException;
}
