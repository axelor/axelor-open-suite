package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineCostPriceComputeService {
  Map<String, Object> computeSubTotalCostPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Product product) throws AxelorException;
}
