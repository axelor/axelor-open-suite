package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;
import java.util.List;

public interface InvoiceTermPfpToolService {
  Integer checkOtherInvoiceTerms(List<InvoiceTerm> invoiceTermList);
}
