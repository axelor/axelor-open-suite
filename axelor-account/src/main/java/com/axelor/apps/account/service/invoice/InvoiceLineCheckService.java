package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface InvoiceLineCheckService {

  void checkTaxLinesNotOnlyNonDeductibleTaxes(List<InvoiceLine> invoiceLineList)
      throws AxelorException;

  void checkSumOfNonDeductibleTaxes(List<InvoiceLine> invoiceLineList) throws AxelorException;
}
