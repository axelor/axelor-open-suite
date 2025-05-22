package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineBlockingSupplychainService;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderBlockingSupplychainServiceImpl
    implements SaleOrderBlockingSupplychainService {

  protected final SaleOrderLineBlockingSupplychainService saleOrderLineBlockingSupplychainService;

  @Inject
  public SaleOrderBlockingSupplychainServiceImpl(
      SaleOrderLineBlockingSupplychainService saleOrderLineBlockingSupplychainService) {
    this.saleOrderLineBlockingSupplychainService = saleOrderLineBlockingSupplychainService;
  }

  @Override
  public boolean hasOnGoingBlocking(SaleOrder saleOrder) {
    Objects.requireNonNull(saleOrder);
    if (saleOrder.getSaleOrderLineList() != null) {
      return saleOrder.getSaleOrderLineList().stream()
          .anyMatch(saleOrderLineBlockingSupplychainService::isDeliveryBlocked);
    }
    return false;
  }
}
