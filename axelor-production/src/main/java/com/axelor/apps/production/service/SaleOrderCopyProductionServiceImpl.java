package com.axelor.apps.production.service;

import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class SaleOrderCopyProductionServiceImpl implements SaleOrderCopyProductionService {

  protected final AppProductionService appProductionService;

  @Inject
  public SaleOrderCopyProductionServiceImpl(AppProductionService appProductionService) {
    this.appProductionService = appProductionService;
  }

  @Override
  public void copySaleOrderProductionProcess(SaleOrder copy) {
    if (!appProductionService.isApp("production")) {
      return;
    }

    if (copy.getSaleOrderLineList() != null) {
      for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
        saleOrderLine.setQtyProduced(BigDecimal.ZERO);
      }
    }
  }
}
