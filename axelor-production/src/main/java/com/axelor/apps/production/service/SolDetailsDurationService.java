package com.axelor.apps.production.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public interface SolDetailsDurationService {
  BigDecimal computeSolDetailsDuration(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrderLine saleOrderLine);
}
