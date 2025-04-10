package com.axelor.apps.supplychain.service.saleorderline.view;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineViewSupplychainService {
  Map<String, Map<String, Object>> hideSupplychainPanels(SaleOrder saleOrder);

  Map<String, Map<String, Object>> hideDeliveredQty(SaleOrder saleOrder);

  Map<String, Map<String, Object>> hideAllocatedQtyBtn(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  Map<String, Map<String, Object>> setAnalyticDistributionPanelHidden(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException;

  Map<String, Map<String, Object>> setReservedQtyReadonly(SaleOrder saleOrder);

  Map<String, Map<String, Object>> setDistributionLineReadonly(SaleOrder saleOrder);
}
