package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.project.db.Project;
import java.util.List;

public interface BusinessProjectProdOrderService {

  List<ProductionOrder> generateProductionOrders(Project project) throws AxelorException;
}
