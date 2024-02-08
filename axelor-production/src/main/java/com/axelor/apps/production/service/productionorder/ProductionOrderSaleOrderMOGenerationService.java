package com.axelor.apps.production.service.productionorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

import java.math.BigDecimal;

public interface ProductionOrderSaleOrderMOGenerationService {

    ProductionOrder generateManufOrders(ProductionOrder productionOrder, SaleOrderLine saleOrderLine, Product product, BigDecimal qtyToProduce) throws AxelorException;

}
