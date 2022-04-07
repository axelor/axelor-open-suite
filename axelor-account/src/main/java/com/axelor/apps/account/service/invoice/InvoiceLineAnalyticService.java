package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface InvoiceLineAnalyticService {

  public InvoiceLine selectDefaultDistributionTemplate(InvoiceLine invoiceLine)
      throws AxelorException;

  InvoiceLine clearAnalyticAccounting(InvoiceLine invoiceLine);

  InvoiceLine analyzeInvoiceLine(InvoiceLine invoiceLine, Invoice invoice) throws AxelorException;

  List<AnalyticMoveLine> getAndComputeAnalyticDistribution(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException;

  List<AnalyticMoveLine> computeAnalyticDistribution(InvoiceLine invoiceLine);

  List<AnalyticMoveLine> createAnalyticDistributionWithTemplate(InvoiceLine invoiceLine);
}
