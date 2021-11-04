package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
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
  protected MoveLineCreateService moveLineCreateService;
  protected AccountingSituationService accountingSituationService;
  protected AccountConfigService accountConfigService;
  protected AccountManagementService accountManagementService;

  @Inject
  public MoveCounterPartServiceImpl(
      MoveRepository moveRepository,
      MoveLineToolService moveLineToolService,
      MoveLineCreateService moveLineCreateService,
      AccountingSituationService accountingSituationService,
      AccountConfigService accountConfigService,
      AccountManagementService accountManagementService) {
    this.moveRepository = moveRepository;
    this.moveLineToolService = moveLineToolService;
    this.moveLineCreateService = moveLineCreateService;
    this.accountingSituationService = accountingSituationService;
    this.accountConfigService = accountConfigService;
    this.accountManagementService = accountManagementService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void generateCounterpartMoveLine(Move move) throws AxelorException {
    move.addMoveLineListItem(createCounterpartMoveLine(move));
    moveRepository.save(move);
  }

  @Override
  public MoveLine createCounterpartMoveLine(Move move) throws AxelorException {
    Account accountingAccount = getAccountingAccountFromJournal(move);
    boolean isDebit;
    BigDecimal amount = getCounterpartAmount(move);
    isDebit = amount.compareTo(BigDecimal.ZERO) > 0;
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
            move.getMoveLineList().size() + 1,
            move.getOrigin(),
            move.getDescription());

    moveLine.setIsOtherCurrency(move.getCurrency().equals(move.getCompanyCurrency()));
    moveLine = moveLineToolService.setCurrencyAmount(moveLine);
    moveLine.setDescription(move.getDescription());
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
