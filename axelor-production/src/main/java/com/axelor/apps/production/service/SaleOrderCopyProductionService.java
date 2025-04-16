package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderCopyProductionService {

  void copySaleOrderProductionProcess(SaleOrder copy);
}
