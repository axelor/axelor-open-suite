package com.axelor.apps.businessproject.service.extraexpense;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.db.ExtraExpenseLine;
import java.util.List;

public interface ExtraExpenseInvoiceService {
  List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ExtraExpenseLine> extraExpenseLineList, int priority)
      throws AxelorException;

  List<InvoiceLine> createInvoiceLine(
      Invoice invoice, ExtraExpenseLine extraExpenseLine, int priority) throws AxelorException;
}
