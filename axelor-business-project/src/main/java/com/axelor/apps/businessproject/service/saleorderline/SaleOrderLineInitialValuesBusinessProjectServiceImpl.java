package com.axelor.apps.businessproject.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.time.LocalDate;

public class SaleOrderLineInitialValuesBusinessProjectServiceImpl
    implements SaleOrderLineInitialValuesBusinessProjectService {

  @Override
  public LocalDate setEstimatedDateValue(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    LocalDate estimatedShippingDate = null;
    if (saleOrder != null || saleOrderLine.getDeliveryState() < 2) {
      estimatedShippingDate = saleOrder.getEstimatedShippingDate();
    } else {
      estimatedShippingDate = saleOrderLine.getEstimatedShippingDate();
    }
    return estimatedShippingDate;
  }
}
