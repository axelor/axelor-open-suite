package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.List;

public interface SaleOrderAdvancePaymentFetchService {
  List<Invoice> getAdvancePayments(SaleOrder saleOrder);
}
