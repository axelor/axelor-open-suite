package com.axelor.apps.production.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderLineBlockingProductionServiceImpl
    implements SaleOrderLineBlockingProductionService {

  protected final AppBaseService appBaseService;

  @Inject
  public SaleOrderLineBlockingProductionServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public boolean isProductionBlocked(SaleOrderLine saleOrderLine) {
    Objects.requireNonNull(saleOrderLine);
    var company = saleOrderLine.getSaleOrder().getCompany();

    if (saleOrderLine.getProductionBlockingToDate() == null) {
      return saleOrderLine.getIsProductionBlocking();
    }

    return saleOrderLine.getIsProductionBlocking()
        && appBaseService
            .getTodayDate(company)
            .isBefore(saleOrderLine.getProductionBlockingToDate());
  }
}
