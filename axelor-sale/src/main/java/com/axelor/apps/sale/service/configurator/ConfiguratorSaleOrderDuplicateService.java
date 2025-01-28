package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;

public interface ConfiguratorSaleOrderDuplicateService {

  void duplicateSaleOrderLine(SaleOrderLine saleOrderLine)
      throws AxelorException, JsonProcessingException;

  void simpleDuplicate(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;

  SaleOrder duplicateSaleOrder(SaleOrder saleOrder) throws AxelorException;

  /**
   * This method will duplicate given list, it does apply methods duplicateSaleOrderLine or
   * simpleDuplicate but it will not compute the sale order.<br>
   * If an error occurs during the duplication of sale order line, a simple copy of the line will be
   * generated instead, and the error traced.
   *
   * @param SaleOrder: saleOrder
   * @return list of SaleOrderLine
   */
  List<SaleOrderLine> duplicateSaleOrderLineList(SaleOrder SaleOrder, boolean deep);
}
