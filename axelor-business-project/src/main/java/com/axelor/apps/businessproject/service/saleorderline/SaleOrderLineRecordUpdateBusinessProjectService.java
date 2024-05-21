package com.axelor.apps.businessproject.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.time.LocalDate;
import java.util.Map;

public interface SaleOrderLineRecordUpdateBusinessProjectService {
  void resetInvoicingMode(SaleOrderLine saleOrderLine);

  void setEstimatedDateValue(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void setProjectValue(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  LocalDate setEstimatedDateValue(
          SaleOrderLine saleOrderLine, SaleOrder saleOrder);
}
