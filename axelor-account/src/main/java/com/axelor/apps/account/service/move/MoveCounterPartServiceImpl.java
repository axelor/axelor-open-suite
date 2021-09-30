package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class MoveCounterPartServiceImpl implements MoveCounterPartService {

  protected MoveRepository moveRepository;
  protected MoveLineToolService moveLineToolService;
  protected MoveLineCreateService moveLineCreateService;

  @Inject
  public MoveCounterPartServiceImpl(
      MoveRepository moveRepository,
      MoveLineToolService moveLineToolService,
      MoveLineCreateService moveLineCreateService) {
    this.moveRepository = moveRepository;
    this.moveLineToolService = moveLineToolService;
    this.moveLineCreateService = moveLineCreateService;
  }

  @Override
  @Transactional
  public void generateCounterpartMoveLine(Move move) throws Exception {
    move.addMoveLineListItem(createCounterpartMoveLine(move));
    moveRepository.save(move);
  }

  @Override
  public MoveLine createCounterpartMoveLine(Move move) throws Exception {

    Account accountingAccount = getAccountingAccountFromJournal(move);
    boolean isDebit;
    BigDecimal amount = getCounterpartAmount(move);
    if (amount.compareTo(BigDecimal.ZERO) == -1) {
      isDebit = false;
    } else {
      isDebit = true;
    }
    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            move.getPartner(),
            accountingAccount,
            BigDecimal.ZERO,
            amount.abs(),
            BigDecimal.ZERO,
            isDebit,
            move.getDate(),
            move.getDate(),
            move.getOriginDate(),
            move.getMoveLineList().size(),
            move.getOrigin(),
            move.getDescription());
    moveLine.setIsOtherCurrency(move.getCurrency().equals(move.getCompanyCurrency()));
    moveLine = moveLineToolService.setCurrencyAmount(moveLine);
    return moveLine;
  }

  protected BigDecimal getCounterpartAmount(Move move) {
    BigDecimal amount = BigDecimal.ZERO;
    for (MoveLine line : move.getMoveLineList()) {
      amount = amount.add(line.getCredit());
      amount = amount.subtract(line.getDebit());
    }
    return amount;
  }

  protected Account getAccountingAccountFromJournal(Move move) {
    Account accountingAccount = null;
    if (move.getJournal()
            .getJournalType()
            .getTechnicalTypeSelect()
            .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE)
        || move.getJournal()
            .getJournalType()
            .getTechnicalTypeSelect()
            .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)) {

      for (AccountingSituation accountingSituation :
          move.getPartner().getAccountingSituationList()) {
        if (accountingSituation.getCompany().equals(move.getCompany())) {
          if (move.getJournal()
              .getJournalType()
              .getTechnicalTypeSelect()
              .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE))
            accountingAccount = accountingSituation.getSupplierAccount();
          else if (move.getJournal()
              .getJournalType()
              .getTechnicalTypeSelect()
              .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE))
            accountingAccount = accountingSituation.getCustomerAccount();
        }
      }
    } else if (move.getJournal()
        .getJournalType()
        .getTechnicalTypeSelect()
        .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY)) {
      if (move.getPaymentMode() != null)
        for (AccountManagement accountManagement :
            move.getPaymentMode().getAccountManagementList()) {
          if (accountManagement.getCompany().equals(move.getCompany())) {
            accountingAccount = accountManagement.getCashAccount();
          }
        }
    }
    if (accountingAccount == null) {
      if (move.getJournal()
          .getJournalType()
          .getTechnicalTypeSelect()
          .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE))
        accountingAccount = move.getCompany().getAccountConfig().getSupplierAccount();
      else if (move.getJournal()
          .getJournalType()
          .getTechnicalTypeSelect()
          .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE))
        accountingAccount = move.getCompany().getAccountConfig().getCustomerAccount();
    }
    return accountingAccount;
  }
}
