package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface InvoiceTermReplaceService {
  void replaceInvoiceTerms(
      Invoice invoice, Move move, List<MoveLine> invoiceMoveLineList, Account partnerAccount)
      throws AxelorException;
}
