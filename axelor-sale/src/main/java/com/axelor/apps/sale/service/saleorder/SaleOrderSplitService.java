package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.Map;

public interface SaleOrderSplitService {
  SaleOrder generateConfirmedSaleOrder(SaleOrder saleOrder, Map<Long, BigDecimal> qtyToOrderMap)
      throws AxelorException;

  void checkSolOrderedQty(SaleOrder saleOrder) throws AxelorException;

  BigDecimal getQtyToOrderLeft(SaleOrderLine saleOrderLine);
}
