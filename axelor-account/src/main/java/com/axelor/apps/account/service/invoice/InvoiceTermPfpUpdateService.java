package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface InvoiceTermPfpUpdateService {

  void updatePfp(
      InvoiceTerm invoiceTerm, Map<InvoiceTerm, Integer> invoiceTermPfpValidateStatusSelectMap)
      throws AxelorException;
}
