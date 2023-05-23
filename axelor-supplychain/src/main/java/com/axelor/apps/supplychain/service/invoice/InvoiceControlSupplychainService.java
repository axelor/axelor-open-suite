package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;

public interface InvoiceControlSupplychainService {
  void checkOrders(Invoice invoice) throws AxelorException;
}
