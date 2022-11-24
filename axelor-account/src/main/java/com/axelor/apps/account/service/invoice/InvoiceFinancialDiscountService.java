package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;

public interface InvoiceFinancialDiscountService {

  /**
   * Set the financial discount informations of the invoice.
   *
   * @param invoice
   */
  void setFinancialDiscountInformations(Invoice invoice);

  /**
   * reset the financial discount informations of the invoice
   *
   * @param invoice
   */
  void resetFinancialDiscountInformations(Invoice invoice);
}
