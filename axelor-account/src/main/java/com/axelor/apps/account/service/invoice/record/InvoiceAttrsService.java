package com.axelor.apps.account.service.invoice.record;

import com.axelor.apps.account.db.Invoice;
import java.util.Map;

public interface InvoiceAttrsService {
  void hideCancelButton(Invoice invoice, Map<String, Map<String, Object>> attrsMap);
}
