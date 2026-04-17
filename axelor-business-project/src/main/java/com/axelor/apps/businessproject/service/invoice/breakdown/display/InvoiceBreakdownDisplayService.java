package com.axelor.apps.businessproject.service.invoice.breakdown.display;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.businessproject.service.invoice.breakdown.BreakdownDisplayLine;
import java.util.List;

public interface InvoiceBreakdownDisplayService {
  /**
   * Generates the structured breakdown for the given invoice. Returns an ordered list of
   * DisplayLine objects ready for rendering.
   */
  List<BreakdownDisplayLine> generateBreakdownFromInvoice(Invoice invoice);
}
