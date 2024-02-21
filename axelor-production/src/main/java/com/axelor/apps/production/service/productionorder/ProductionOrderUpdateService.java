package com.axelor.apps.production.service.productionorder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;

public interface ProductionOrderUpdateService {

  ProductionOrder addManufOrder(ProductionOrder productionOrder, ManufOrder manufOrder);

  ProductionOrder updateProductionOrderStatus(ProductionOrder productionOrder);
}
