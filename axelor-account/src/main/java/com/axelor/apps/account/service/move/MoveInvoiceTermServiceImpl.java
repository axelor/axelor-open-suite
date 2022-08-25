package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MoveInvoiceTermServiceImpl implements MoveInvoiceTermService {
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected InvoiceTermService invoiceTermService;
  protected MoveRepository moveRepo;
  protected InvoiceTermRepository invoiceTermRepo;

  @Inject
  public MoveInvoiceTermServiceImpl(
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      InvoiceTermService invoiceTermService,
      MoveRepository moveRepo,
      InvoiceTermRepository invoiceTermRepo) {
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.invoiceTermService = invoiceTermService;
    this.moveRepo = moveRepo;
    this.invoiceTermRepo = invoiceTermRepo;
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
            .filter(invoiceTermService::isNotReadonly)
            .collect(Collectors.toList());

    invoiceTermToUpdateList.forEach(it -> invoiceTermService.updateFromMoveHeader(move, it));

    return invoiceTermToUpdateList.size()
        == move.getMoveLineList().stream()
            .map(MoveLine::getInvoiceTermList)
            .mapToLong(Collection::size)
            .sum();
  }

  @Override
  public void recreateInvoiceTerms(Move move) throws AxelorException {
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        if (moveLine.getAccount().getHasInvoiceTerm()) {
          moveLineInvoiceTermService.recreateInvoiceTerms(moveLine);
        }
      }
    }
  }

  public void updateMoveLineDueDates(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLine.setDueDate(
          invoiceTermService.getDueDate(moveLine.getInvoiceTermList(), moveLine.getOriginDate()));
    }
  }

  @Override
  public boolean displayDueDate(Move move) {
    return move.getJournal() != null
        && move.getJournal().getJournalType().getTechnicalTypeSelect()
            != JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
        && this.isSingleTerm(move);
  }

  protected boolean isSingleTerm(Move move) {
    if (move.getPaymentCondition() == null
        || move.getPaymentCondition().getPaymentConditionLineList().size() > 1) {
      return false;
    }

    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      List<MoveLine> moveLinesWithInvoiceTerms =
          move.getMoveLineList().stream()
              .filter(it -> it.getAccount().getHasInvoiceTerm())
              .collect(Collectors.toList());

      return moveLinesWithInvoiceTerms.size() <= 1
          && (moveLinesWithInvoiceTerms.size() == 0
              || moveLinesWithInvoiceTerms.get(0).getInvoiceTermList().size() <= 1);
    }

    return true;
  }

  @Override
  public LocalDate computeDueDate(Move move, boolean isSingleTerm, boolean isDateChange) {
    if (move.getPaymentCondition() == null
        || CollectionUtils.isEmpty(move.getPaymentCondition().getPaymentConditionLineList())
        || move.getPaymentCondition().getPaymentConditionLineList().size() > 1) {
      return null;
    }

    if (isSingleTerm && !isDateChange && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      InvoiceTerm singleInvoiceTerm = this.getSingleInvoiceTerm(move);

      if (singleInvoiceTerm != null) {
        return singleInvoiceTerm.getDueDate();
      }
    }

    return invoiceTermService.computeDueDate(
        move, move.getPaymentCondition().getPaymentConditionLineList().get(0));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateSingleInvoiceTermDueDate(Move move, LocalDate dueDate) {
    if (CollectionUtils.isEmpty(move.getMoveLineList()) || dueDate == null) {
      return;
    }

    InvoiceTerm singleInvoiceTerm = this.getSingleInvoiceTerm(move);

    if (singleInvoiceTerm != null
        && invoiceTermService.isNotReadonly(singleInvoiceTerm)
        && !Objects.equals(dueDate, singleInvoiceTerm.getDueDate())) {
      singleInvoiceTerm.setDueDate(dueDate);
      singleInvoiceTerm.getMoveLine().setDueDate(dueDate);
      invoiceTermRepo.save(singleInvoiceTerm);
    }
  }

  protected InvoiceTerm getSingleInvoiceTerm(Move move) {
    return move.getMoveLineList().stream()
        .filter(it -> it.getAccount().getHasInvoiceTerm())
        .map(MoveLine::getInvoiceTermList)
        .flatMap(Collection::stream)
        .findFirst()
        .orElse(null);
  }
}
