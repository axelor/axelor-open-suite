package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface ConfiguratorSaleOrderDuplicateService {

  void duplicateSaleOrderLine(SaleOrderLine saleOrderLine)
      throws AxelorException, JsonProcessingException;

  void simpleDuplicate(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;

  SaleOrder duplicateSaleOrder(SaleOrder saleOrder) throws AxelorException;
}
