package com.axelor.apps.account.service.invoice.tax;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;

public interface InvoiceLineTaxRecordService {
  BigDecimal computeInTaxTotal(InvoiceLineTax invoiceLineTax);

  BigDecimal computeCompanyTaxTotal(InvoiceLineTax invoiceLineTax, Invoice invoice)
      throws AxelorException;

  BigDecimal computeCompanyInTaxTotal(InvoiceLineTax invoiceLineTax);
}
