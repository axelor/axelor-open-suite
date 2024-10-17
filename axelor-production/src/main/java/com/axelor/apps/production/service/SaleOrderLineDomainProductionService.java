package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineDomainProductionService {
  String getBomDomain(SaleOrderLine saleOrderLine);

  String getProdProcessDomain(SaleOrderLine saleOrderLine);
}
