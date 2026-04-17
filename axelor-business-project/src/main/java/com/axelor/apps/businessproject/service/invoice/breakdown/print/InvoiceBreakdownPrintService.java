package com.axelor.apps.businessproject.service.invoice.breakdown.print;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.businessproject.service.invoice.breakdown.BreakdownDisplayLine;
import java.util.List;

public interface InvoiceBreakdownPrintService {
  String printInvoiceBreakdown(Invoice invoice) throws Exception;

  String buildHtmlFromData(List<BreakdownDisplayLine> lines);
}
