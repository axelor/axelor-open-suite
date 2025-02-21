package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import java.util.List;
import java.util.Set;

public interface InvoiceLineCheckService {

  void checkTaxLinesNotOnlyNonDeductibleTaxes(List<InvoiceLine> invoiceLineList)
      throws AxelorException;

  void checkSumOfNonDeductibleTaxes(List<InvoiceLine> invoiceLineList) throws AxelorException;

  void checkInvoiceLineTaxes(Set<TaxLine> taxLineSet) throws AxelorException;
}
