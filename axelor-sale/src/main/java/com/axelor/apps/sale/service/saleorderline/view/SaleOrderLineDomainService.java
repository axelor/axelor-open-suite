package com.axelor.apps.sale.service.saleorderline.view;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineDomainService {

  /**
   * Compute product domain from configurations and sale order.
   *
   * @param saleOrderLine a sale order line
   * @param saleOrder a sale order (can be a sale order from context and not from database)
   * @param isSubLine specify wether the current saleOrderLine is a sub line or not
   * @return a String with the JPQL expression used to filter product selection
   */
  String computeProductDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder, boolean isSubLine);
}
