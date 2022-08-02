package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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

  @Override
  public void roundInvoiceTermPercentages(Move move) {
    move.getMoveLineList().stream()
        .filter(it -> CollectionUtils.isNotEmpty(it.getInvoiceTermList()))
        .forEach(
            it ->
                invoiceTermService.roundPercentages(
                    it.getInvoiceTermList(), it.getDebit().max(it.getCredit())));
  }

  @Override
  public boolean updateInvoiceTerms(Move move) {
    List<InvoiceTerm> invoiceTermToUpdateList =
        move.getMoveLineList().stream()
            .filter(
                it ->
                    it.getAmountRemaining().compareTo(it.getDebit().max(it.getCredit())) == 0
                        && it.getAccount().getHasInvoiceTerm()
                        && CollectionUtils.isNotEmpty(it.getInvoiceTermList()))
            .map(MoveLine::getInvoiceTermList)
            .flatMap(Collection::stream)
            .filter(
                it ->
                    !it.getIsPaid()
                        && it.getAmountRemaining().compareTo(it.getAmount()) == 0
                        && invoiceTermService.isNotAwaitingPayment(it))
            .collect(Collectors.toList());

    invoiceTermToUpdateList.forEach(it -> invoiceTermService.updateFromMoveHeader(move, it));

    return invoiceTermToUpdateList.size()
        == move.getMoveLineList().stream()
            .map(MoveLine::getInvoiceTermList)
            .mapToLong(Collection::size)
            .sum();
  }
}
