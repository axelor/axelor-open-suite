package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.SaleOrder;

public interface ConfiguratorCheckService {

  void checkLinkedSaleOrderLine(Configurator configurator, Product product) throws AxelorException;

  void checkLinkedSaleOrderLine(Configurator configurator) throws AxelorException;

  void checkHaveConfigurator(SaleOrder saleOrder) throws AxelorException;
}
