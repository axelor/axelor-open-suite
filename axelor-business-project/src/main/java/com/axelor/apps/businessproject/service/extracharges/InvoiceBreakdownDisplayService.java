package com.axelor.apps.businessproject.service.extracharges;

import com.axelor.apps.account.db.Invoice;
import java.util.List;
import java.util.Map;

public interface InvoiceBreakdownDisplayService {
  List<Map<String, Object>> generateBreakdownFromInvoice(Invoice invoice);
}
