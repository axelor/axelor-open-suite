package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.rpc.Context;
import java.util.Map;

public interface SaleOrderLineService {
  SaleOrder getSaleOrder(Context context);

  Map<String, Object> emptyLine(SaleOrderLine saleOrderLine);
}
