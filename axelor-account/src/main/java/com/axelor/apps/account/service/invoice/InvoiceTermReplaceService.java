package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;

public interface InvoiceTermReplaceService {
  void replaceInvoiceTerms(Invoice invoice, Move move) throws AxelorException;
}
