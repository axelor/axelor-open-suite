package com.axelor.apps.account.service.debtrecovery;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface DoubtfulCustomerInvoiceTermService {
  void createOrUpdateInvoiceTerms(
      Invoice invoice,
      Move newMove,
      MoveLine invoicePartnerMoveLine,
      MoveLine creditMoveLine,
      MoveLine debitMoveLine,
      LocalDate todayDate,
      BigDecimal amountRemaining)
      throws AxelorException;
}
