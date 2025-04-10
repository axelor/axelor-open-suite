package com.axelor.apps.supplychain.service.saleorderline.view;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineSupplychainOnNewService {

  Map<String, Map<String, Object>> getSupplychainOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;
}
