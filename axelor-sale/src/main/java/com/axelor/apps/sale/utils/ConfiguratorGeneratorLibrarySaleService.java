package com.axelor.apps.sale.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface ConfiguratorGeneratorLibrarySaleService {
  SaleOrderLine createSaleOrderLineFromConfigurator(Map<String, Object> params)
      throws AxelorException;
}
