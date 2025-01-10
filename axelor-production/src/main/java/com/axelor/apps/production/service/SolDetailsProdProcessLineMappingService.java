package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;

public interface SolDetailsProdProcessLineMappingService {
  SaleOrderLineDetails mapToSaleOrderLineDetails(ProdProcessLine prodProcessLine);
}
