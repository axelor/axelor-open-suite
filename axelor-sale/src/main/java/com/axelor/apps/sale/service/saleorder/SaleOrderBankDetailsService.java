package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderBankDetailsService {
  Map<String, Object> getBankDetails(SaleOrder saleOrder) throws AxelorException;
}
