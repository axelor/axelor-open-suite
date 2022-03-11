package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface InvoiceLineAnalyticService {

  public InvoiceLine selectDefaultDistributionTemplate(InvoiceLine invoiceLine)
      throws AxelorException;

  InvoiceLine clearAnalyticAccounting(InvoiceLine invoiceLine);

  InvoiceLine checkAnalyticMoveLineForAxis(InvoiceLine invoiceLine);

  InvoiceLine analyzeInvoiceLine(InvoiceLine invoiceLine, Invoice invoice) throws AxelorException;

  InvoiceLine printAnalyticAccount(InvoiceLine invoiceLine, Company company) throws AxelorException;

  List<Long> getAxisDomains(InvoiceLine invoiceLine, Invoice invoice, int position)
      throws AxelorException;

  List<AnalyticMoveLine> getAndComputeAnalyticDistribution(InvoiceLine invoiceLine, Invoice invoice)
      throws AxelorException;

  List<AnalyticMoveLine> computeAnalyticDistribution(InvoiceLine invoiceLine);

  List<AnalyticMoveLine> createAnalyticDistributionWithTemplate(InvoiceLine invoiceLine);

  boolean isAxisRequired(InvoiceLine invoiceLine, int position) throws AxelorException;
}
