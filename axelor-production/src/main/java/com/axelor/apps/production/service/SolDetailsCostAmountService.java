package com.axelor.apps.production.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public interface SolDetailsCostAmountService {

  BigDecimal computeSolDetailsMachineCostAmount(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrderLine saleOrderLine);

  BigDecimal computeSolDetailsHumanCostAmount(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrderLine saleOrderLine);
}
