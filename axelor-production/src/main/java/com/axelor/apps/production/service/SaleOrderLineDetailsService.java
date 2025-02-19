package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderLineDetailsService {

  Map<String, Object> productOnChange(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder) throws AxelorException;
}
