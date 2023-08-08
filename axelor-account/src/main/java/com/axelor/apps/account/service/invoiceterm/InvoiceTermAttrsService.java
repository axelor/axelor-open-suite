package com.axelor.apps.account.service.invoiceterm;

import com.axelor.apps.account.db.InvoiceTerm;
import java.util.Map;

public interface InvoiceTermAttrsService {
  void hideActionAndPfpPanel(InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap);

  void changeAmountsTitle(InvoiceTerm invoiceTerm, Map<String, Map<String, Object>> attrsMap);
}
