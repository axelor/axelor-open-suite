package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.rpc.ActionResponse;

public interface SaleOrderLineMultipleQtyService {

  void checkMultipleQty(SaleOrderLine saleOrderLine, ActionResponse response);
}
