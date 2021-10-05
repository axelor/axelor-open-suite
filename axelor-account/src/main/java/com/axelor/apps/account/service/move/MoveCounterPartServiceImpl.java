package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class MoveCounterPartServiceImpl implements MoveCounterPartService {

  protected MoveRepository moveRepository;
  protected MoveLineToolService moveLineToolService;
  protected AccountingSituationService accountingSituationService;
  protected AccountConfigService accountConfigService;
  protected AccountManagementService accountManagementService;

  @Inject
  public MoveCounterPartServiceImpl(
      MoveRepository moveRepository,
      MoveLineToolService moveLineToolService,
      AccountingSituationService accountingSituationService,
      AccountConfigService accountConfigService,
      AccountManagementService accountManagementService) {
    this.moveRepository = moveRepository;
    this.moveLineToolService = moveLineToolService;
    this.accountingSituationService = accountingSituationService;
    this.accountConfigService = accountConfigService;
    this.accountManagementService = accountManagementService;
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
    Company company = move.getCompany();
    int technicalTypeSelect = move.getJournal().getJournalType().getTechnicalTypeSelect();
    if (technicalTypeSelect == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE) {
      accountingAccount =
          accountingSituationService.getSupplierAccount(move.getPartner(), move.getCompany());
    } else if (technicalTypeSelect == JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE) {
      accountingAccount =
          accountingSituationService.getCustomerAccount(move.getPartner(), move.getCompany());
    } else if (technicalTypeSelect == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
        && move.getPaymentMode() != null) {
      AccountManagement accountManagement =
          accountManagementService.getAccountManagement(
              move.getPaymentMode().getAccountManagementList(), company);
      if (ObjectUtils.notEmpty(accountManagement)) {
        accountingAccount = accountManagement.getCashAccount();
      }
    }
    return accountingAccount;
  }
}
