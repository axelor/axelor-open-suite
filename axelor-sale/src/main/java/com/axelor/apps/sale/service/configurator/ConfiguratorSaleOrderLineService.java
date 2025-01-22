package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface ConfiguratorSaleOrderLineService {
  void regenerateSaleOrderLines(Configurator configurator, Product product) throws AxelorException;

  void regenerateSaleOrderLine(
      Configurator configurator, Product product, SaleOrderLine saleOrderLine)
      throws AxelorException;
}
