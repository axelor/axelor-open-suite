package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderDateService {

  SaleOrder computeEndOfValidityDate(SaleOrder saleOrder);
}
