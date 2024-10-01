package com.axelor.apps.sale.service.saleorderline.creation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineInitValueService {
  Map<String, Object> onNewInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  Map<String, Object> onLoadInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  Map<String, Object> onNewEditableInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine);
}
