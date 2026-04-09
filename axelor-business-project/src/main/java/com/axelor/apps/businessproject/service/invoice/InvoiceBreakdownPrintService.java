package com.axelor.apps.businessproject.service.invoice;

import com.axelor.apps.account.db.Invoice;
import java.util.List;
import java.util.Map;

public interface InvoiceBreakdownPrintService {
  String printInvoiceBreakdown(Invoice invoice) throws Exception;

  String buildHtmlFromData(List<Map<String, Object>> data);
}
