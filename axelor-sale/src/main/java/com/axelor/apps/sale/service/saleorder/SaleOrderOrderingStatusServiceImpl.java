package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import java.math.BigDecimal;
import java.util.List;

public class SaleOrderOrderingStatusServiceImpl implements SaleOrderOrderingStatusService {
  @Override
  public void updateOrderingStatus(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList.stream()
        .allMatch(line -> line.getQty().compareTo(line.getOrderedQty()) == 0)) {
      saleOrder.setOrderingStatus(SaleOrderRepository.ORDERING_STATUS_CLOSED);
    } else if (saleOrderLineList.stream()
        .anyMatch(line -> line.getOrderedQty().compareTo(BigDecimal.ZERO) > 0)) {
      saleOrder.setOrderingStatus(SaleOrderRepository.ORDERING_STATUS_PARTIALLY_ORDERED);
    }
  }
}
