package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.model.AnalyticLineModel;

public interface InvoiceLineAnalyticSupplychainService extends InvoiceLineAnalyticService {

  void setInvoiceLineAnalyticInfo(
      InvoiceLine invoiceLine, Invoice invoice, AnalyticLineModel analyticLineModel)
      throws AxelorException;
}
