package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderInitValueService {
  Map<String, Object> getOnNewInitValues(SaleOrder saleOrder) throws AxelorException;
}
