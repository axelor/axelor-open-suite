package com.axelor.apps.businessproject.service;

import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;

public class SaleOrderCopyProjectServiceImpl implements SaleOrderCopyProjectService {

  protected final AppBusinessProjectService appBusinessProjectService;

  @Inject
  public SaleOrderCopyProjectServiceImpl(AppBusinessProjectService appBusinessProjectService) {
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Override
  public void copySaleOrderProjectProcess(SaleOrder copy) {
    if (appBusinessProjectService.isApp("business-project")) {
      copy.setProject(null);
      if (copy.getSaleOrderLineList() != null) {
        for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
          saleOrderLine.setProject(null);
        }
      }
    }
  }
}
