package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.rpc.Context;
import java.util.List;
import java.util.Map;

public interface MultiLevelSaleOrderLineService {

  SaleOrderLine setSOLineStartValues(SaleOrderLine saleOrderLine, Context context);

  List<SaleOrderLine> updateRelatedLines(SaleOrderLine dirtyLine, SaleOrder saleOrder)
      throws AxelorException;

  SaleOrderLine findDirtyLine(List<Map<String, Object>> list);
}
