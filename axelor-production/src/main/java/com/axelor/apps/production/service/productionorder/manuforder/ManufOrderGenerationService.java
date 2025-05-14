package com.axelor.apps.production.service.productionorder.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ManufOrderGenerationService {

  ManufOrder generateManufOrder(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      LocalDateTime endDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      ManufOrderService.ManufOrderOriginType manufOrderOriginType,
      ManufOrder manufOrderParent)
      throws AxelorException;
}
