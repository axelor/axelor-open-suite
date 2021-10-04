package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class MoveCounterPartServiceImpl implements MoveCounterPartService {

  protected MoveRepository moveRepository;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public MoveCounterPartServiceImpl(
      MoveRepository moveRepository, MoveLineToolService moveLineToolService) {
    this.moveRepository = moveRepository;
    this.moveLineToolService = moveLineToolService;
  }

  @Override
  @Transactional
  public void generateCounterpartMoveLine(Move move) {
    move.addMoveLineListItem(createCounterpartMoveLine(move));
    moveRepository.save(move);
  }

  @Override
  public MoveLine createCounterpartMoveLine(Move move) {
    MoveLine moveLine = new MoveLine();
    moveLine.setMove(moveRepository.find(move.getId()));
    moveLine.setDate(move.getDate());
    moveLine.setOrigin(move.getOrigin());
    moveLine.setOriginDate(move.getOriginDate());
    moveLine.setDescription(move.getDescription());
    moveLine.setPartner(move.getPartner());
    moveLine.setIsOtherCurrency(move.getCurrency().equals(move.getCompanyCurrency()));

    Account accountingAccount = getAccountingAccountFromJournal(move);

    if (accountingAccount != null) moveLine.setAccount(accountingAccount);

    BigDecimal amount = getCounterpartAmount(move);
    if (amount.compareTo(BigDecimal.ZERO) == -1) {
      moveLine.setCredit(amount.abs());
    } else {
      moveLine.setDebit(amount.abs());
    }

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
