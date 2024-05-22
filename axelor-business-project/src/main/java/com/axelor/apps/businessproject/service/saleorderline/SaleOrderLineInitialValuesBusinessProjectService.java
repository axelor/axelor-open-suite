package com.axelor.apps.businessproject.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.time.LocalDate;

public interface SaleOrderLineInitialValuesBusinessProjectService {
  LocalDate setEstimatedDateValue(SaleOrderLine saleOrderLine, SaleOrder saleOrder);
}
