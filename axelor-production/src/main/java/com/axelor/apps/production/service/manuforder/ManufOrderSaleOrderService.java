package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public interface ManufOrderSaleOrderService {

  ProductionOrder generateManufOrders(ProductionOrder productionOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  /**
   * Compute quantity left to produce for a sale order line depending on the stock move lines
   * present in the manuf order of the line. Also including the quantity of draft manuf order
   * (considered as planned qty) since they do not have stock move line generated.
   *
   * @param saleOrderLine
   * @return
   */
  BigDecimal computeQuantityToProduceLeft(SaleOrderLine saleOrderLine);
}
