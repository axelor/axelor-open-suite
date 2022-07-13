package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.commons.collections.CollectionUtils;

public class MoveLineInvoiceTermServiceImpl implements MoveLineInvoiceTermService {
  protected InvoiceTermService invoiceTermService;

  @Inject
  public MoveLineInvoiceTermServiceImpl(InvoiceTermService invoiceTermService) {
    this.invoiceTermService = invoiceTermService;
  }

  @Override
  public void generateDefaultInvoiceTerm(MoveLine moveLine) {
    Move move = moveLine.getMove();
    BigDecimal amount = moveLine.getCredit().max(moveLine.getDebit());
    LocalDate dueDate =
        move.getPaymentCondition() == null || move.getOriginDate() == null
            ? move.getDate()
            : InvoiceToolService.getDueDate(
                move.getPaymentCondition().getPaymentConditionLineList().get(0),
                move.getOriginDate());

    invoiceTermService.createInvoiceTerm(
        null,
        moveLine,
        moveLine.getMove().getPartnerBankDetails(),
        null,
        move.getPaymentMode(),
        dueDate,
        null,
        amount,
        BigDecimal.valueOf(100),
        false);
  }

  public void updateInvoiceTermsParentFields(MoveLine moveLine) {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      moveLine
          .getInvoiceTermList()
          .forEach(it -> invoiceTermService.setParentFields(it, moveLine, it.getInvoice()));
    }
  }
}
