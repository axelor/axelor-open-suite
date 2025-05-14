package com.axelor.apps.production.service.productionorder.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SaleOrderLineMOGenerationSingleLineService {

  ProductionOrder generateManufOrders(
      ProductionOrder productionOrder,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine)
      throws AxelorException;
}
