package com.axelor.apps.account.service.invoice.attributes;

import com.axelor.apps.account.db.Invoice;
import java.util.Map;

public interface InvoiceAttrsService {

  void setInvoiceLineScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap);

  void setInvoiceLineTaxScale(Invoice invoice, Map<String, Map<String, Object>> attrsMap);
}
