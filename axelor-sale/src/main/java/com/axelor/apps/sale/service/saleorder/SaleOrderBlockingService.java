package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderBlockingService {

  boolean hasOngoingBlockingDeliveries(SaleOrder saleOrder);

  boolean hasOngoingBlockingDeliveries(SaleOrderLine saleOrderLine, Company company);
}
