package com.axelor.apps.production.service.productionorder.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public interface SaleOrderLineMOGenerationService {

  ProductionOrder generateManufOrders(
      SaleOrderLine saleOrderLine,
      SaleOrder saleOrder,
      ProductionOrder productionOrder,
      BigDecimal qtyRequested)
      throws AxelorException;
}
