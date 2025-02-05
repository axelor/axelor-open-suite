package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;

public interface InvoiceTermDateComputeService {
  LocalDate getInvoiceDateForTermGeneration(Invoice invoice) throws AxelorException;

  void computeDueDateValues(InvoiceTerm invoiceTerm, LocalDate invoiceDate);

  void resetDueDate(InvoiceTerm invoiceTerm) throws AxelorException;
}
