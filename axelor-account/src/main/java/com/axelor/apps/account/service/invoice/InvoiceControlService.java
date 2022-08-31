package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;

public interface InvoiceControlService {

  /**
   * Method that checks if there is a duplicate of invoice by looking for invoice that shares
   * supplierInvoiceNb, partner and year of originDate.
   *
   * @param invoice
   * @return true if duplicate, false else.
   */
  Boolean isDuplicate(Invoice invoice);
}
