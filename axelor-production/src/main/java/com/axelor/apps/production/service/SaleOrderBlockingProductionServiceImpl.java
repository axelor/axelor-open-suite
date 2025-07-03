package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderBlockingProductionServiceImpl implements SaleOrderBlockingProductionService {

  protected final SaleOrderLineBlockingProductionService saleOrderLineBlockingProductionService;

  @Inject
  public SaleOrderBlockingProductionServiceImpl(
      SaleOrderLineBlockingProductionService saleOrderLineBlockingProductionService) {
    this.saleOrderLineBlockingProductionService = saleOrderLineBlockingProductionService;
  }

  @Override
  public boolean hasOnGoingBlocking(SaleOrder saleOrder) {
    Objects.requireNonNull(saleOrder);
    if (saleOrder.getSaleOrderLineList() != null) {
      return saleOrder.getSaleOrderLineList().stream()
          .anyMatch(saleOrderLineBlockingProductionService::isProductionBlocked);
    }
    return false;
  }
}
