package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderLineBlockingSupplychainServiceImpl
    implements SaleOrderLineBlockingSupplychainService {

  protected final AppBaseService appBaseService;

  @Inject
  public SaleOrderLineBlockingSupplychainServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public boolean isDeliveryBlocked(SaleOrderLine saleOrderLine) {
    Objects.requireNonNull(saleOrderLine);
    var company = saleOrderLine.getSaleOrder().getCompany();

    if (saleOrderLine.getDeliveryBlockingToDate() == null) {
      return saleOrderLine.getIsDeliveryBlocking();
    }

    return saleOrderLine.getIsDeliveryBlocking()
        && appBaseService.getTodayDate(company).isBefore(saleOrderLine.getDeliveryBlockingToDate());
  }
}
