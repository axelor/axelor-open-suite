package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.base.db.Company;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface InvoiceLineTaxToolService {
  List<Pair<InvoiceLineTax, Account>> getInvoiceLineTaxAccountPair(
      List<InvoiceLineTax> invoiceLineTaxList, Company company);
}
