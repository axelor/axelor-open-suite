package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class MoveInvoiceTermServiceImpl implements MoveInvoiceTermService {
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected InvoiceTermService invoiceTermService;
  protected MoveRepository moveRepo;

  @Inject
  public MoveInvoiceTermServiceImpl(
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      InvoiceTermService invoiceTermService,
      MoveRepository moveRepo) {
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.invoiceTermService = invoiceTermService;
    this.moveRepo = moveRepo;
  }

  @Override
  public void generateInvoiceTerms(Move move) throws AxelorException {
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        if (moveLine.getAccount() != null
            && moveLine.getAccount().getUseForPartnerBalance()
            && CollectionUtils.isEmpty(moveLine.getInvoiceTermList())) {
          moveLineInvoiceTermService.generateDefaultInvoiceTerm(moveLine, false);
        }
      }
    }
  }

  public void roundInvoiceTermPercentages(Move move) {
    move.getMoveLineList().stream()
        .filter(it -> CollectionUtils.isNotEmpty(it.getInvoiceTermList()))
        .forEach(
            it ->
                invoiceTermService.roundPercentages(
                    it.getInvoiceTermList(), it.getDebit().max(it.getCredit())));
  }
}
