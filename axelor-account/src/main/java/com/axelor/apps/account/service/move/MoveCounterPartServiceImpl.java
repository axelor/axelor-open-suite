package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class MoveCounterPartServiceImpl implements MoveCounterPartService {

  protected MoveRepository moveRepository;
  protected MoveLineToolService moveLineToolService;
  protected AccountingSituationService accountingSituationService;
  protected AccountConfigService accountConfigService;

  @Inject
  public MoveCounterPartServiceImpl(
      MoveRepository moveRepository,
      MoveLineToolService moveLineToolService,
      AccountingSituationService accountingSituationService,
      AccountConfigService accountConfigService) {
    this.moveRepository = moveRepository;
    this.moveLineToolService = moveLineToolService;
    this.accountingSituationService = accountingSituationService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  @Transactional
  public void generateCounterpartMoveLine(Move move) throws AxelorException {
    move.addMoveLineListItem(createCounterpartMoveLine(move));
    moveRepository.save(move);
  }

  @Override
  public MoveLine createCounterpartMoveLine(Move move) throws AxelorException {
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

  protected Account getAccountingAccountFromJournal(Move move) throws AxelorException {
    Account accountingAccount = null;
    if (move.getJournal()
            .getJournalType()
            .getTechnicalTypeSelect()
            .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE)
        || move.getJournal()
            .getJournalType()
            .getTechnicalTypeSelect()
            .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)) {

      AccountingSituation accountingSituation =
          accountingSituationService.getAccountingSituation(move.getPartner(), move.getCompany());
      if (move.getJournal()
          .getJournalType()
          .getTechnicalTypeSelect()
          .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE)) {
        accountingAccount = accountingSituation.getSupplierAccount();
      } else if (move.getJournal()
          .getJournalType()
          .getTechnicalTypeSelect()
          .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)) {
        accountingAccount = accountingSituation.getCustomerAccount();
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
      AccountConfig accountConfig = accountConfigService.getAccountConfig(move.getCompany());
      if (move.getJournal()
          .getJournalType()
          .getTechnicalTypeSelect()
          .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE))
        accountingAccount = accountConfigService.getSupplierAccount(accountConfig);
      else if (move.getJournal()
          .getJournalType()
          .getTechnicalTypeSelect()
          .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE))
        accountingAccount = accountConfigService.getCustomerAccount(accountConfig);
    }
    return accountingAccount;
  }
}
