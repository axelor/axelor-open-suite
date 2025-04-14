package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderBlockingServiceImpl implements SaleOrderBlockingService {

  protected final BlockingService blockingService;

  @Inject
  public SaleOrderBlockingServiceImpl(BlockingService blockingService) {
    this.blockingService = blockingService;
  }

  @Override
  public boolean hasOngoingBlockingDeliveries(SaleOrder saleOrder) {

    if (saleOrder == null) {
      return false;
    }

    if (saleOrder.getSaleOrderLineList() != null) {
      return saleOrder.getSaleOrderLineList().stream()
          .anyMatch(sol -> hasOngoingBlockingDeliveries(sol, saleOrder.getCompany()));
    }

    return false;
  }

  @Override
  public boolean hasOngoingBlockingDeliveries(SaleOrderLine saleOrderLine, Company company) {
    if (saleOrderLine == null) {
      return false;
    }

    if (saleOrderLine.getBlockingList() != null) {
      return saleOrderLine.getBlockingList().stream()
          .filter(Objects::nonNull)
          .anyMatch(blocking -> blockingService.isOngoing(blocking, company));
    }

    return false;
  }
}
