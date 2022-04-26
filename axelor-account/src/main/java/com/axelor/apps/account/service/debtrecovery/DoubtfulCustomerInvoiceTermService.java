package com.axelor.apps.account.service.debtrecovery;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DoubtfulCustomerInvoiceTermService {
  void createOrUpdateInvoiceTerms(
      Invoice invoice,
      Move newMove,
      List<MoveLine> invoicePartnerMoveLines,
      List<MoveLine> creditMoveLines,
      MoveLine debitMoveLine,
      LocalDate todayDate,
      BigDecimal amountRemaining)
      throws AxelorException;
}
