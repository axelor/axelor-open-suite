package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineViewService {
  Map<String, Map<String, Object>> getOnNewAttrs(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAttrs(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Map<String, Object>> getProductOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;

  Map<String, Map<String, Object>> getDiscountTypeSelectOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  Map<String, Map<String, Object>> hidePriceDiscounted(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  Map<String, Map<String, Object>> getDiscountAmountTitle(SaleOrderLine saleOrderLine);

  Map<String, Map<String, Object>> getPriceAndQtyScale();
}
