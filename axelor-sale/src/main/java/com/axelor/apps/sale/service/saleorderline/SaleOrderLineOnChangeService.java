package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineOnChangeService {
  Map<String, Object> qtyOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Object> taxLineOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Object> priceOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Object> inTaxPriceOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Object> typeSelectOnChange(SaleOrderLine saleOrderLine);

  Map<String, Object> productOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Object> compute(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;
}
