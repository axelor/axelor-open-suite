package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
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
  public void generateInvoiceTerms(Move move) {
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      move.getMoveLineList().stream()
          .filter(
              it ->
                  it.getAccount() != null
                      && it.getAccount().getUseForPartnerBalance()
                      && CollectionUtils.isEmpty(it.getInvoiceTermList()))
          .forEach(moveLineInvoiceTermService::generateDefaultInvoiceTerm);
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
