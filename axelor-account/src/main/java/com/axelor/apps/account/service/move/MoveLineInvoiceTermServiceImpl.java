package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import java.math.BigDecimal;

public class MoveLineInvoiceTermServiceImpl implements MoveLineInvoiceTermService {
  @Override
  public void generateDefaultInvoiceTerm(MoveLine moveLine) {
    InvoiceTerm invoiceTerm = new InvoiceTerm();

    BigDecimal amount =
        moveLine.getCredit().signum() == 0 ? moveLine.getDebit() : moveLine.getCredit();

    invoiceTerm.setSequence(1);
    invoiceTerm.setIsPaid(false);
    invoiceTerm.setIsHoldBack(false);
    invoiceTerm.setAmount(amount);
    invoiceTerm.setAmountRemaining(amount);
    invoiceTerm.setPercentage(BigDecimal.valueOf(100));
    invoiceTerm.setPaymentMode(moveLine.getMove().getPaymentMode());
    invoiceTerm.setDueDate(moveLine.getDueDate());

    moveLine.addInvoiceTermListItem(invoiceTerm);
  }
}
