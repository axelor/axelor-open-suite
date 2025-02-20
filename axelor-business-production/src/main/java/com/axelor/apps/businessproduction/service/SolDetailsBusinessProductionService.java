package com.axelor.apps.businessproduction.service;

import com.axelor.apps.sale.db.SaleOrder;

public interface SolDetailsBusinessProductionService {
  void copySolDetailsList(SaleOrder saleOrder);

  void deleteSolDetailsList(SaleOrder saleOrder);
}
