package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface ConfiguratorSaleOrderLineService {
  void regenerateSaleOrderLines(Configurator configurator, Product product) throws AxelorException;

  /**
   * Regenerates sale order line, it will saleOrderLine from its saleOrder list and replace it with
   * the new generated one.
   *
   * @param configurator
   * @param product
   * @param saleOrderLine
   * @throws AxelorException
   */
  void regenerateSaleOrderLine(
      Configurator configurator, Product product, SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  SaleOrderLine generateSaleOrderLine(
      Configurator configurator, Product product, SaleOrderLine saleOrderLine)
      throws AxelorException;
}
