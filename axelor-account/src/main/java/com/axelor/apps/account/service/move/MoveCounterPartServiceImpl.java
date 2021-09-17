package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoveCounterPartServiceImpl implements MoveCounterPartService {
	  protected static final int RETURNED_SCALE = 2;

  protected MoveRepository moveRepository;
  protected MoveLineToolService moveLineToolService;
  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;

  @Inject
  public MoveCounterPartServiceImpl(
      MoveRepository moveRepository, MoveLineToolService moveLineToolService,AccountConfigService accountConfigService, AppAccountService appAccountService) {
    this.moveRepository = moveRepository;
    this.moveLineToolService = moveLineToolService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
  }

  @Override
  @Transactional
  public void generateCounterpartMoveLine(Move move) throws AxelorException {
	    MoveLine counterPartMoveLine = createCounterpartMoveLine(move);
	    move.addMoveLineListItem(counterPartMoveLine);
	    generateCounterpartAnalyticMoveLine(move, counterPartMoveLine);
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

      for (int i = 0; i < move.getPartner().getAccountingSituationList().size(); i++) {
        if (move.getPartner().getAccountingSituationList().get(i).equals(move.getCompany())) {
          if (move.getJournal()
              .getJournalType()
              .getTechnicalTypeSelect()
              .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE))
            accountingAccount =
                move.getPartner().getAccountingSituationList().get(i).getSupplierAccount();
          else if (move.getJournal()
              .getJournalType()
              .getTechnicalTypeSelect()
              .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE))
            accountingAccount =
                move.getPartner().getAccountingSituationList().get(i).getCustomerAccount();
        }
      }
    } else if (move.getJournal()
        .getJournalType()
        .getTechnicalTypeSelect()
        .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY)) {
      if (move.getPaymentMode() != null)
        for (int i = 0; i < move.getPaymentMode().getAccountManagementList().size(); i++) {
          if (move.getPaymentMode()
              .getAccountManagementList()
              .get(0)
              .getCompany()
              .equals(move.getCompany())) {
            accountingAccount =
                move.getPaymentMode().getAccountManagementList().get(0).getCashAccount();
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
  
  @Override
  public void generateCounterpartAnalyticMoveLine(Move move, MoveLine counterpartMoveLine)
	      throws AxelorException {
	    List<AnalyticAccount> analyticAccounts = getAnalyticAccountList(move);
	    List<AnalyticMoveLine> analyticMoveLines = getAnalyticMoveLines(move);
	    counterpartMoveLine.clearAnalyticMoveLineList();
	    if (appAccountService.getAppAccount().getManageAnalyticAccounting()
	        && move.getCompany().getAccountConfig().getManageAnalyticAccounting()
	        && hasToCreateCounterpartAnalytics(move)) {
	      for (AnalyticAccount analyticAccount : analyticAccounts) {
	        counterpartMoveLine.addAnalyticMoveLineListItem(
	            createAnalyticMoveLine(analyticAccount, analyticMoveLines, counterpartMoveLine));
	      }

	      if (counterpartMoveLine.getAnalyticMoveLineList() != null) {
	        counterpartMoveLine =
	            computeCounterpartAnalyticMoveLines(counterpartMoveLine, analyticMoveLines);
	      }
	    }
	  }
  
  protected List<AnalyticAccount> getAnalyticAccountList(Move move) {
	    List<AnalyticAccount> analyticAccounts = new ArrayList<AnalyticAccount>();
	    for (MoveLine moveLine : move.getMoveLineList()) {
	      if (moveLine.getAnalyticMoveLineList() != null) {
	        for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
	          if (!analyticAccounts.contains(analyticMoveLine.getAnalyticAccount())) {
	            analyticAccounts.add(analyticMoveLine.getAnalyticAccount());
	          }
	        }
	      }
	    }
	    return analyticAccounts;
	  }

  protected boolean hasToCreateCounterpartAnalytics(Move move) {
    boolean result = true;
    int analyticMoveLineAmount = 0;
    if (move.getJournal() == null || move.getJournal() != null && 
    		(move.getJournal().getJournalType() == null || move.getJournal().getJournalType() != null && (!(move.getJournal()
            .getJournalType()
            .getCode()
            .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE)
        || move.getJournal()
            .getJournalType()
            .getCode()
            .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE))))) {
      result = false;
    }
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAnalyticMoveLineList() != null) {
        analyticMoveLineAmount += moveLine.getAnalyticMoveLineList().size();
      }
    }
    if (analyticMoveLineAmount == 0) {
      result = false;
    }
    return result;
  }

  protected MoveLine computeCounterpartAnalyticMoveLines(
      MoveLine counterpartMoveLine, List<AnalyticMoveLine> analyticMoveLines) {
    BigDecimal totalAmount = BigDecimal.ZERO;
    BigDecimal counterpartAmount =
        counterpartMoveLine.getDebit().add(counterpartMoveLine.getCredit());
    BigDecimal analyticMoveLineAmount = BigDecimal.ZERO;

    for (AnalyticMoveLine analyticMoveLine : analyticMoveLines) {
      totalAmount = totalAmount.add(analyticMoveLine.getAmount());
    }
    for (AnalyticMoveLine analyticMoveLine : counterpartMoveLine.getAnalyticMoveLineList()) {
      analyticMoveLineAmount = analyticMoveLine.getAmount();
      analyticMoveLine.setAmount(
          counterpartAmount
              .multiply(analyticMoveLineAmount)
              .divide(totalAmount, RETURNED_SCALE, RoundingMode.HALF_UP));
      analyticMoveLine.setPercentage(
          analyticMoveLineAmount.divide(totalAmount, RETURNED_SCALE, RoundingMode.HALF_UP));
    }
    return counterpartMoveLine;
  }

  protected AnalyticMoveLine createAnalyticMoveLine(
      AnalyticAccount analyticAccount,
      List<AnalyticMoveLine> analyticMoveLines,
      MoveLine counterpartMoveLine)
      throws AxelorException {
    List<AnalyticMoveLine> accountAnalyticMoveLines =
        analyticMoveLines.stream()
            .filter(aml -> aml.getAnalyticAccount().equals(analyticAccount))
            .collect(Collectors.toList());
    AnalyticMoveLine analyticMoveLine = new AnalyticMoveLine();
    analyticMoveLine.setAnalyticAccount(analyticAccount);
    if (counterpartMoveLine.getAccount() != null) {
      analyticMoveLine.setAccount(counterpartMoveLine.getAccount());
      analyticMoveLine.setAccountType(counterpartMoveLine.getAccount().getAccountType());
    }
    analyticMoveLine.setAmount(BigDecimal.ZERO);

    for (AnalyticMoveLine aml : accountAnalyticMoveLines) {
      analyticMoveLine.setAmount(analyticMoveLine.getAmount().add(aml.getAmount()));
    }
    analyticMoveLine.setDate(counterpartMoveLine.getDate());

    if (counterpartMoveLine.getMove() != null
        && accountConfigService
                .getAccountConfig(counterpartMoveLine.getMove().getCompany())
                .getAnalyticJournal()
            != null) {

      analyticMoveLine.setAnalyticJournal(
          accountConfigService
              .getAccountConfig(analyticAccount.getAnalyticAxis().getCompany())
              .getAnalyticJournal());
    }
    AnalyticJournal analyticJournal = analyticMoveLine.getAnalyticJournal();
    Company company = analyticJournal == null ? null : analyticJournal.getCompany();
    if (company != null) {
      analyticMoveLine.setCurrency(company.getCurrency());
    }

    analyticMoveLine.setAnalyticAxis(analyticAccount.getAnalyticAxis());

    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING);

    analyticMoveLine.setMoveLine(counterpartMoveLine);
    return analyticMoveLine;
  }

  protected List<AnalyticMoveLine> getAnalyticMoveLines(Move move) {
    List<AnalyticMoveLine> analyticMoveLines = new ArrayList<AnalyticMoveLine>();
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAnalyticMoveLineList() != null) {
        for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
          analyticMoveLines.add(analyticMoveLine);
        }
      }
    }
    return analyticMoveLines;
  }
}
