package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface InvoiceTermFilterService {

  /**
   * Method that returns only unpaid invoice terms of an invoice having holdback status same as
   * first returned invoice term
   *
   * @param invoice
   * @return
   */
  List<InvoiceTerm> getUnpaidInvoiceTermsFiltered(Invoice invoice) throws AxelorException;

  List<InvoiceTerm> getUnpaidInvoiceTermsFilteredWithoutPfpCheck(Invoice invoice)
      throws AxelorException;

  /**
   * Method that filters invoiceTerm List and returns only invoice terms with holdback status same
   * as first invoice term of the list.
   *
   * @param invoiceTerms
   * @return
   */
  List<InvoiceTerm> filterInvoiceTermsByHoldBack(List<InvoiceTerm> invoiceTerms);

  List<InvoiceTerm> getUnpaidInvoiceTermsWithoutPfpCheck(Invoice invoice) throws AxelorException;

  /**
   * Method that returns unpaid invoiceTerms (isPaid != true) of an invoice
   *
   * @param invoice
   * @return
   */
  List<InvoiceTerm> getUnpaidInvoiceTerms(Invoice invoice) throws AxelorException;

  List<InvoiceTerm> filterNotAwaitingPayment(List<InvoiceTerm> invoiceTermList);

  boolean isNotAwaitingPayment(InvoiceTerm invoiceTerm);
}
