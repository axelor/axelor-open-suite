package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;

public interface InvoiceValidationService {
  void checkNotOnlyNonDeductibleTaxes(Invoice invoice) throws AxelorException;
}
