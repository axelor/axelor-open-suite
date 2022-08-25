/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountManagementRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.bankpayment.db.BankPaymentConfig;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.BankStatementRule;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementQueryRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.report.IReport;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.BankReconciliationLoadService;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.afb120.BankReconciliationLoadAFB120Service;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.tool.StringTool;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BankReconciliationService {
  protected static final int RETURNED_SCALE = 2;

  protected AccountManagementRepository accountManagementRepository;
  protected AccountService accountService;
  protected BankReconciliationRepository bankReconciliationRepository;
  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;
  protected BankStatementQueryRepository bankStatementQueryRepository;
  protected MoveLineRepository moveLineRepository;
  protected MoveRepository moveRepository;
  protected BankStatementLineRepository bankStatementLineRepository;
  protected BankStatementRuleRepository bankStatementRuleRepository;
  protected MoveValidateService moveValidateService;
  protected PeriodService periodService;
  protected BankReconciliationLineService bankReconciliationLineService;
  protected MoveLineService moveLineService;
  protected BankReconciliationLineRepository bankReconciliationLineRepository;
  protected MoveLineToolService moveLineToolService;
  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected BankDetailsService bankDetailsService;
  protected BankReconciliationLoadAFB120Service bankReconciliationLoadAFB120Service;
  protected BankReconciliationLoadService bankReconciliationLoadService;
  protected JournalRepository journalRepository;
  protected AccountRepository accountRepository;
  protected AccountConfigRepository accountConfigRepository;
  protected BankPaymentConfigService bankPaymentConfigService;

  @Inject
  public BankReconciliationService(
      BankReconciliationRepository bankReconciliationRepository,
      AccountService accountService,
      AccountManagementRepository accountManagementRepository,
      BankStatementQueryRepository bankStatementQueryRepository,
      MoveLineRepository moveLineRepository,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository,
      MoveRepository moveRepository,
      BankStatementLineRepository bankStatementLineRepository,
      BankStatementRuleRepository bankStatementRuleRepository,
      PeriodService periodService,
      MoveValidateService moveValidateService,
      BankReconciliationLineService bankReconciliationLineService,
      MoveLineService moveLineService,
      BankReconciliationLineRepository bankReconciliationLineRepository,
      MoveLineToolService moveLineToolService,
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      BankDetailsService bankDetailsService,
      BankReconciliationLoadAFB120Service bankReconciliationLoadAFB120Service,
      BankReconciliationLoadService bankReconciliationLoadService,
      JournalRepository journalRepository,
      AccountRepository accountRepository,
      AccountConfigRepository accountConfigRepository,
      BankPaymentConfigService bankPaymentConfigService) {

    this.bankReconciliationRepository = bankReconciliationRepository;
    this.accountService = accountService;
    this.accountManagementRepository = accountManagementRepository;
    this.bankStatementQueryRepository = bankStatementQueryRepository;
    this.moveLineRepository = moveLineRepository;
    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
    this.moveRepository = moveRepository;
    this.bankStatementLineRepository = bankStatementLineRepository;
    this.bankStatementRuleRepository = bankStatementRuleRepository;
    this.moveValidateService = moveValidateService;
    this.periodService = periodService;
    this.bankReconciliationLineService = bankReconciliationLineService;
    this.moveLineService = moveLineService;
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
    this.moveLineToolService = moveLineToolService;
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.bankDetailsService = bankDetailsService;
    this.bankReconciliationLoadAFB120Service = bankReconciliationLoadAFB120Service;
    this.bankReconciliationLoadService = bankReconciliationLoadService;
    this.journalRepository = journalRepository;
    this.accountRepository = accountRepository;
    this.accountConfigRepository = accountConfigRepository;
    this.bankPaymentConfigService = bankPaymentConfigService;
  }

  public void generateMovesAutoAccounting(BankReconciliation bankReconciliation)
      throws AxelorException {
    Context scriptContext;
    Move move;
    int limit = 10;
    int offset = 0;
    List<BankReconciliationLine> bankReconciliationLines =
        bankReconciliationLineRepository
            .findByBankReconciliation(bankReconciliation)
            .fetch(limit, offset);

    List<BankStatementRule> bankStatementRules;
    while (bankReconciliationLines.size() > 0) {
      for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
        if (bankReconciliationLine.getMoveLine() != null) {
          continue;
        }
        scriptContext =
            new Context(
                Mapper.toMap(bankReconciliationLine.getBankStatementLine()),
                BankStatementLineAFB120.class);
        bankStatementRules =
            bankStatementRuleRepository
                .all()
                .filter(
                    "self.ruleTypeSelect = :ruleTypeSelect"
                        + " AND self.accountManagement.interbankCodeLine = :interbankCodeLine"
                        + " AND self.accountManagement.company = :company"
                        + " AND self.accountManagement.bankDetails = :bankDetails")
                .bind("ruleTypeSelect", BankStatementRuleRepository.RULE_TYPE_ACCOUNTING_AUTO)
                .bind(
                    "interbankCodeLine",
                    bankReconciliationLine.getBankStatementLine().getOperationInterbankCodeLine())
                .bind("company", bankReconciliationLine.getBankReconciliation().getCompany())
                .bind("bankDetails", bankReconciliationLine.getBankStatementLine().getBankDetails())
                .fetch();

        for (BankStatementRule bankStatementRule : bankStatementRules) {

          if (bankStatementRule != null
              && bankStatementRule.getBankStatementQuery() != null
              && !Strings.isNullOrEmpty(bankStatementRule.getBankStatementQuery().getQuery())
              && Boolean.TRUE.equals(
                  new GroovyScriptHelper(scriptContext)
                      .eval(
                          bankStatementRule
                              .getBankStatementQuery()
                              .getQuery()
                              .replaceAll(
                                  "%s", "\"" + bankStatementRule.getSearchLabel() + "\"")))) {
            if (bankStatementRule.getAccountManagement().getJournal() == null) {
              continue;
            }
            move = generateMove(bankReconciliationLine, bankStatementRule);
            moveValidateService.validate(move);
            break;
          }
        }
      }
      offset += limit;
      JPA.clear();
      bankReconciliationLines =
          bankReconciliationLineRepository
              .findByBankReconciliation(bankReconciliation)
              .fetch(limit, offset);
    }
  }

  @Transactional
  public Move generateMove(
      BankReconciliationLine bankReconciliationLine, BankStatementRule bankStatementRule)
      throws AxelorException {
    String description = "";
    description =
        description.concat(bankReconciliationLine.getBankStatementLine().getDescription());
    description = StringTool.cutTooLongString(description);

    if (!Strings.isNullOrEmpty(bankReconciliationLine.getReference())) {
      String reference = "ref:";
      reference =
          StringTool.cutTooLongString(reference.concat(bankReconciliationLine.getReference()));
      description = StringTool.cutTooLongStringWithOffset(description, reference.length());
      description = description.concat(reference);
    }
    AccountManagement accountManagement = bankStatementRule.getAccountManagement();
    Move move =
        moveCreateService.createMove(
            accountManagement.getJournal(),
            accountManagement.getCompany(),
            bankReconciliationLine.getBankStatementLine().getCurrency(),
            bankStatementRule.getPartner(),
            bankReconciliationLine.getEffectDate(),
            bankReconciliationLine.getEffectDate(),
            accountManagement.getPaymentMode(),
            bankStatementRule.getPartner() != null
                ? bankStatementRule.getPartner().getFiscalPosition()
                : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            bankReconciliationLine.getBankStatementLine().getOrigin(),
            description);

    MoveLine moveLine = generateMoveLine(bankReconciliationLine, bankStatementRule, move, true);
    bankReconciliationLineService.reconcileBRLAndMoveLine(bankReconciliationLine, moveLine);
    moveLine = generateMoveLine(bankReconciliationLine, bankStatementRule, move, false);
    moveLine = moveLineRepository.save(moveLine);
    return moveRepository.save(move);
  }

  protected MoveLine generateMoveLine(
      BankReconciliationLine bankReconciliationLine,
      BankStatementRule bankStatementRule,
      Move move,
      boolean isFirstLine)
      throws AxelorException {
    MoveLine moveLine;
    LocalDate date = bankReconciliationLine.getEffectDate();
    BigDecimal debit;
    BigDecimal credit;
    Account account;
    String description = move.getDescription();
    String origin = move.getOrigin();
    if (isFirstLine) {
      debit = bankReconciliationLine.getCredit();
      credit = bankReconciliationLine.getDebit();
      account = bankStatementRule.getAccountManagement().getCashAccount();
      if (account == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.BANK_STATEMENT_RULE_CASH_ACCOUNT_MISSING),
            bankStatementRule.getSearchLabel());
      }
    } else {
      debit = bankReconciliationLine.getDebit();
      credit = bankReconciliationLine.getCredit();
      account = bankStatementRule.getCounterpartAccount();
      if (account == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.BANK_STATEMENT_RULE_COUNTERPART_ACCOUNT_MISSING),
            bankStatementRule.getSearchLabel());
      }
    }
    boolean isDebit = debit.compareTo(credit) > 0;
    moveLine =
        moveLineCreateService.createMoveLine(
            move,
            move.getPartner(),
            account,
            debit.add(credit),
            isDebit,
            date,
            date,
            move.getMoveLineList().size() + 1,
            origin,
            description);
    move.addMoveLineListItem(moveLine);
    return moveLine;
  }

  public BankReconciliation computeBalances(BankReconciliation bankReconciliation) {
    int limit = 10;
    int offset = 0;
    List<BankReconciliation> bankReconciliations;
    List<MoveLine> moveLines;

    BigDecimal statementReconciledLineBalance = BigDecimal.ZERO;
    BigDecimal movesReconciledLineBalance = BigDecimal.ZERO;
    BigDecimal statementUnreconciledLineBalance = BigDecimal.ZERO;
    BigDecimal movesUnreconciledLineBalance = BigDecimal.ZERO;
    BigDecimal statementOngoingReconciledBalance = BigDecimal.ZERO;
    BigDecimal movesOngoingReconciledBalance = BigDecimal.ZERO;

    bankReconciliations =
        bankReconciliationRepository
            .findByBankDetails(bankReconciliation.getBankDetails())
            .order("id")
            .fetch(limit, offset);
    if (bankReconciliations.size() != 0) {
      statementReconciledLineBalance =
          statementReconciledLineBalance.add(bankReconciliations.get(0).getStartingBalance());
      movesReconciledLineBalance =
          movesReconciledLineBalance.add(bankReconciliations.get(0).getStartingBalance());
    }
    do {
      for (BankReconciliation br : bankReconciliations) {
        for (BankReconciliationLine brl : br.getBankReconciliationLineList()) {
          statementReconciledLineBalance =
              computeStatementReconciledLineBalance(statementReconciledLineBalance, brl);
          statementUnreconciledLineBalance =
              computeStatementUnreconciledLineBalance(statementUnreconciledLineBalance, brl);
          statementOngoingReconciledBalance =
              computeStatementOngoingReconciledLineBalance(statementOngoingReconciledBalance, brl);
          movesOngoingReconciledBalance =
              computeMovesOngoingReconciledLineBalance(movesOngoingReconciledBalance, brl);
        }
      }
      offset += limit;
      JPA.clear();
      bankReconciliations =
          bankReconciliationRepository
              .findByBankDetails(bankReconciliation.getBankDetails())
              .fetch(limit, offset);
    } while (bankReconciliations.size() != 0);

    offset = 0;
    JPA.clear();
    bankReconciliation = bankReconciliationRepository.find(bankReconciliation.getId());
    moveLines =
        moveLineRepository
            .all()
            .filter("self.account = :cashAccount")
            .bind("cashAccount", bankReconciliation.getCashAccount())
            .fetch(limit, offset);
    do {
      for (MoveLine moveLine : moveLines) {
        movesReconciledLineBalance =
            computeMovesReconciledLineBalance(movesReconciledLineBalance, moveLine);
        movesUnreconciledLineBalance =
            computeMovesUnreconciledLineBalance(movesUnreconciledLineBalance, moveLine);
      }
      offset += limit;
      JPA.clear();
      bankReconciliation = bankReconciliationRepository.find(bankReconciliation.getId());
      moveLines =
          moveLineRepository
              .all()
              .filter("self.account = :cashAccount")
              .bind("cashAccount", bankReconciliation.getCashAccount())
              .fetch(limit, offset);
    } while (moveLines.size() != 0);
    JPA.clear();
    bankReconciliation = bankReconciliationRepository.find(bankReconciliation.getId());
    Account cashAccount = bankReconciliation.getCashAccount();
    if (cashAccount != null) {
      bankReconciliation.setAccountBalance(
          accountService.computeBalance(cashAccount, AccountService.BALANCE_TYPE_DEBIT_BALANCE));
    }
    bankReconciliation.setStatementReconciledLineBalance(statementReconciledLineBalance);
    bankReconciliation.setMovesReconciledLineBalance(movesReconciledLineBalance);
    bankReconciliation.setStatementUnreconciledLineBalance(statementUnreconciledLineBalance);
    bankReconciliation.setMovesUnreconciledLineBalance(movesUnreconciledLineBalance);
    bankReconciliation.setStatementOngoingReconciledBalance(statementOngoingReconciledBalance);
    bankReconciliation.setMovesOngoingReconciledBalance(movesOngoingReconciledBalance);
    bankReconciliation.setStatementAmountRemainingToReconcile(
        statementUnreconciledLineBalance.subtract(statementOngoingReconciledBalance));
    bankReconciliation.setMovesAmountRemainingToReconcile(
        movesUnreconciledLineBalance.subtract(movesOngoingReconciledBalance));
    bankReconciliation.setStatementTheoreticalBalance(
        statementReconciledLineBalance.add(statementOngoingReconciledBalance));
    bankReconciliation.setMovesTheoreticalBalance(
        movesReconciledLineBalance.add(movesOngoingReconciledBalance));
    bankReconciliation = computeEndingBalance(bankReconciliation);
    return saveBR(bankReconciliation);
  }

  protected BigDecimal computeMovesReconciledLineBalance(
      BigDecimal movesReconciledLineBalance, MoveLine moveLine) {
    if (moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0) { // Debit line
      movesReconciledLineBalance =
          movesReconciledLineBalance.add(moveLine.getBankReconciledAmount());
    } else { // Credit line
      movesReconciledLineBalance =
          movesReconciledLineBalance.subtract(moveLine.getBankReconciledAmount());
    }
    return movesReconciledLineBalance;
  }

  protected BigDecimal computeMovesUnreconciledLineBalance(
      BigDecimal movesUnreconciledLineBalance, MoveLine moveLine) {
    if (moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0) { // Debit line
      movesUnreconciledLineBalance =
          movesUnreconciledLineBalance.add(
              moveLine.getDebit().subtract(moveLine.getBankReconciledAmount()));
    } else { // Credit line
      movesUnreconciledLineBalance =
          movesUnreconciledLineBalance.subtract(
              moveLine.getCredit().subtract(moveLine.getBankReconciledAmount()));
    }
    return movesUnreconciledLineBalance;
  }

  protected BigDecimal computeStatementReconciledLineBalance(
      BigDecimal statementReconciledLineBalance, BankReconciliationLine brl) {
    if (brl.getIsPosted()) {

      statementReconciledLineBalance = statementReconciledLineBalance.subtract(brl.getDebit());
      statementReconciledLineBalance = statementReconciledLineBalance.add(brl.getCredit());
    }
    return statementReconciledLineBalance;
  }

  protected BigDecimal computeStatementUnreconciledLineBalance(
      BigDecimal statementUnreconciledLineBalance, BankReconciliationLine brl) {
    if (!brl.getIsPosted()) {
      statementUnreconciledLineBalance = statementUnreconciledLineBalance.subtract(brl.getDebit());
      statementUnreconciledLineBalance = statementUnreconciledLineBalance.add(brl.getCredit());
    }
    return statementUnreconciledLineBalance;
  }

  protected BigDecimal computeStatementOngoingReconciledLineBalance(
      BigDecimal statementOngoingReconciledBalance, BankReconciliationLine brl) {
    if (!brl.getIsPosted() && !Strings.isNullOrEmpty(brl.getPostedNbr())) {
      statementOngoingReconciledBalance =
          statementOngoingReconciledBalance.subtract(brl.getDebit());
      statementOngoingReconciledBalance = statementOngoingReconciledBalance.add(brl.getCredit());
    }
    return statementOngoingReconciledBalance;
  }

  protected BigDecimal computeMovesOngoingReconciledLineBalance(
      BigDecimal movesOngoingReconciledBalance, BankReconciliationLine brl) {
    MoveLine tempMoveLine;
    if (!brl.getIsPosted() && !Strings.isNullOrEmpty(brl.getPostedNbr())) {
      tempMoveLine = brl.getMoveLine();
      if (tempMoveLine != null) {
        if (tempMoveLine.getDebit().compareTo(BigDecimal.ZERO) != 0) {
          movesOngoingReconciledBalance =
              movesOngoingReconciledBalance.add(brl.getCredit().add(brl.getDebit()));
        } else {
          movesOngoingReconciledBalance =
              movesOngoingReconciledBalance.subtract(brl.getCredit().add(brl.getDebit()));
        }
      }
    }
    return movesOngoingReconciledBalance;
  }

  public void compute(BankReconciliation bankReconciliation) {
    BigDecimal totalPaid = BigDecimal.ZERO;
    BigDecimal totalCashed = BigDecimal.ZERO;

    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {
      totalPaid = totalPaid.add(bankReconciliationLine.getDebit());
      totalCashed = totalCashed.add(bankReconciliationLine.getCredit());
    }
    bankReconciliation.setComputedBalance(
        bankReconciliation.getAccountBalance().add(totalCashed).subtract(totalPaid));

    bankReconciliation.setTotalPaid(totalPaid);
    bankReconciliation.setTotalCashed(totalCashed);
    saveBR(bankReconciliation);
  }

  @Transactional
  public BankReconciliation saveBR(BankReconciliation bankReconciliation) {
    return bankReconciliationRepository.save(bankReconciliation);
  }

  public String createDomainForBankDetails(BankReconciliation bankReconciliation) {

    return bankDetailsService.getActiveCompanyBankDetails(
        bankReconciliation.getCompany(), bankReconciliation.getCurrency());
  }

  @Transactional
  public void loadBankStatement(BankReconciliation bankReconciliation) {
    loadBankStatement(bankReconciliation, true);
  }

  @Transactional
  public void loadBankStatement(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();

    BankStatementFileFormat bankStatementFileFormat = bankStatement.getBankStatementFileFormat();

    switch (bankStatementFileFormat.getStatementFileFormatSelect()) {
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_REP:
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM:
        bankReconciliationLoadAFB120Service.loadBankStatement(
            bankReconciliation, includeBankStatement);
        break;

      default:
        bankReconciliationLoadService.loadBankStatement(bankReconciliation, includeBankStatement);
    }

    compute(bankReconciliation);

    bankReconciliationRepository.save(bankReconciliation);
  }

  public String getJournalDomain(BankReconciliation bankReconciliation) {

    String journalIds = null;
    Set<String> journalIdSet = new HashSet<String>();

    journalIdSet.addAll(getAccountManagementJournals(bankReconciliation));

    if (bankReconciliation.getBankDetails().getJournal() != null) {
      journalIdSet.add(bankReconciliation.getBankDetails().getJournal().getId().toString());
    }

    journalIds = String.join(",", journalIdSet);

    return journalIds;
  }

  protected Set<String> getAccountManagementJournals(BankReconciliation bankReconciliation) {
    Set<String> journalIdSet = new HashSet<String>();
    Account cashAccount = bankReconciliation.getCashAccount();
    List<AccountManagement> accountManagementList = new ArrayList<>();

    if (cashAccount != null) {
      accountManagementList =
          accountManagementRepository
              .all()
              .filter(
                  "self.bankDetails = ?1 and self.cashAccount = ?2",
                  bankReconciliation.getBankDetails(),
                  cashAccount)
              .fetch();
    } else {
      accountManagementList =
          accountManagementRepository
              .all()
              .filter("self.bankDetails = ?1", bankReconciliation.getBankDetails())
              .fetch();
    }

    for (AccountManagement accountManagement : accountManagementList) {
      if (accountManagement.getJournal() != null) {
        journalIdSet.add(accountManagement.getJournal().getId().toString());
      }
    }
    return journalIdSet;
  }

  public Journal getJournal(BankReconciliation bankReconciliation) {

    Journal journal = null;
    String journalIds = String.join(",", getAccountManagementJournals(bankReconciliation));
    if (bankReconciliation.getBankDetails().getJournal() != null) {
      journal = bankReconciliation.getBankDetails().getJournal();
    } else if (!Strings.isNullOrEmpty(journalIds) && (journalIds.split(",").length) == 1) {
      journal = journalRepository.find(Long.parseLong(journalIds));
    }
    return journal;
  }

  public String getCashAccountDomain(BankReconciliation bankReconciliation) {

    String cashAccountIds = null;
    Set<String> cashAccountIdSet = new HashSet<String>();

    cashAccountIdSet.addAll(getAccountManagementCashAccounts(bankReconciliation));

    if (bankReconciliation.getBankDetails().getBankAccount() != null) {
      cashAccountIdSet.add(bankReconciliation.getBankDetails().getBankAccount().getId().toString());
    }

    cashAccountIds = String.join(",", cashAccountIdSet);

    return cashAccountIds;
  }

  protected Set<String> getAccountManagementCashAccounts(BankReconciliation bankReconciliation) {
    List<AccountManagement> accountManagementList;
    Journal journal = bankReconciliation.getJournal();
    Set<String> cashAccountIdSet = new HashSet<String>();
    BankDetails bankDetails = bankReconciliation.getBankDetails();

    if (journal != null) {
      accountManagementList =
          accountManagementRepository
              .all()
              .filter("self.bankDetails = ?1 AND self.journal = ?2", bankDetails, journal)
              .fetch();
    } else {
      accountManagementList =
          accountManagementRepository.all().filter("self.bankDetails = ?1", bankDetails).fetch();
    }

    for (AccountManagement accountManagement : accountManagementList) {
      if (accountManagement.getCashAccount() != null) {
        cashAccountIdSet.add(accountManagement.getCashAccount().getId().toString());
      }
    }
    return cashAccountIdSet;
  }

  public Account getCashAccount(BankReconciliation bankReconciliation) {

    Account cashAccount = null;
    String cashAccountIds = String.join(",", getAccountManagementCashAccounts(bankReconciliation));
    if (bankReconciliation.getBankDetails().getBankAccount() != null) {
      cashAccount = bankReconciliation.getBankDetails().getBankAccount();

    } else if (!Strings.isNullOrEmpty(cashAccountIds) && (cashAccountIds.split(",").length) == 1) {
      cashAccount = accountRepository.find(Long.parseLong(cashAccountIds));
    }
    return cashAccount;
  }

  public String getAccountDomain(BankReconciliation bankReconciliation) {
    if (bankReconciliation != null) {
      String domain = "self.id != 0";
      if (bankReconciliation.getCompany() != null) {
        domain = domain.concat(" AND self.company.id = " + bankReconciliation.getCompany().getId());
      }
      if (bankReconciliation.getCashAccount() != null) {
        domain = domain.concat(" AND self.id != " + bankReconciliation.getCashAccount().getId());
      }
      if (bankReconciliation.getJournal() != null
          && !CollectionUtils.isEmpty(bankReconciliation.getJournal().getValidAccountTypeSet())) {
        domain =
            domain.concat(
                " AND (self.accountType.id IN "
                    + bankReconciliation.getJournal().getValidAccountTypeSet().stream()
                        .map(AccountType::getId)
                        .map(id -> id.toString())
                        .collect(Collectors.joining("','", "('", "')"))
                        .toString());
      } else {
        domain = domain.concat(" AND (self.accountType.id = 0");
      }
      if (bankReconciliation.getJournal() != null
          && !CollectionUtils.isEmpty(bankReconciliation.getJournal().getValidAccountSet())) {
        domain =
            domain.concat(
                " OR self.id IN "
                    + bankReconciliation.getJournal().getValidAccountSet().stream()
                        .map(Account::getId)
                        .map(id -> id.toString())
                        .collect(Collectors.joining("','", "('", "')"))
                        .toString()
                    + ")");
      } else {
        domain = domain.concat(" OR self.id = 0)");
      }
      return domain;
    }
    return "self.id = 0";
  }

  public String getRequestMoveLines(BankReconciliation bankReconciliation) {
    String query =
        "(self.date >= :fromDate OR self.dueDate >= :fromDate)"
            + " AND (self.date <= :toDate OR self.dueDate <= :toDate)"
            + " AND self.move.statusSelect != :statusSelect"
            + " AND ((self.debit > 0 AND self.bankReconciledAmount < self.debit)"
            + " OR (self.credit > 0 AND self.bankReconciledAmount < self.credit))";
    if (bankReconciliation.getJournal() != null) {
      query = query + " AND self.move.journal = :journal";
    }
    if (bankReconciliation.getCashAccount() != null) {
      query = query + " AND self.account = :cashAccount";
    } else {
      if (bankReconciliation.getJournal() != null) {
        query = query + " AND self.account.accountType.technicalTypeSelect = :accountType";
      }
    }
    return query;
  }

  public Map<String, Object> getBindRequestMoveLine(BankReconciliation bankReconciliation)
      throws AxelorException {
    Map<String, Object> params = new HashMap<>();
    BankPaymentConfig bankPaymentConfig =
        bankPaymentConfigService.getBankPaymentConfig(bankReconciliation.getCompany());
    int dateMargin = bankPaymentConfig.getBnkStmtAutoReconcileDateMargin();
    params.put("fromDate", bankReconciliation.getFromDate().minusDays(dateMargin));
    params.put("toDate", bankReconciliation.getToDate().plusDays(dateMargin));
    params.put("statusSelect", MoveRepository.STATUS_CANCELED);
    if (bankReconciliation.getJournal() != null) {
      params.put("journal", bankReconciliation.getJournal());
    }
    if (bankReconciliation.getCashAccount() != null) {
      params.put("cashAccount", bankReconciliation.getCashAccount());
    } else if (bankReconciliation.getJournal() != null) {
      params.put("accountType", AccountTypeRepository.TYPE_CASH);
    }
    return params;
  }

  @Transactional
  public BankReconciliation reconciliateAccordingToQueries(BankReconciliation bankReconciliation)
      throws AxelorException {
    List<BankStatementQuery> bankStatementQueries =
        bankStatementQueryRepository
            .findByRuleTypeSelect(BankStatementRuleRepository.RULE_TYPE_RECONCILIATION_AUTO)
            .fetch();
    List<BankReconciliationLine> bankReconciliationLines =
        bankReconciliation.getBankReconciliationLineList();
    List<MoveLine> moveLines =
        moveLineRepository
            .all()
            .filter(getRequestMoveLines(bankReconciliation))
            .bind(getBindRequestMoveLine(bankReconciliation))
            .fetch();
    BigInteger dateMargin =
        BigInteger.valueOf(
            bankReconciliation
                .getCompany()
                .getBankPaymentConfig()
                .getBnkStmtAutoReconcileDateMargin());
    BigDecimal amountMargin =
        bankReconciliation
            .getCompany()
            .getBankPaymentConfig()
            .getBnkStmtAutoReconcileAmountMargin()
            .divide(BigDecimal.valueOf(100), RETURNED_SCALE, RoundingMode.HALF_UP);
    BigDecimal amountMarginLow = BigDecimal.ONE.subtract(amountMargin);
    BigDecimal amountMarginHigh = BigDecimal.ONE;
    bankReconciliationLines =
        bankReconciliationLines.stream()
            .filter(line -> line.getMoveLine() == null)
            .collect(Collectors.toList());
    Context scriptContext;
    for (BankStatementQuery bankStatementQuery : bankStatementQueries) {
      for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
        if (bankReconciliationLine.getMoveLine() != null) {
          continue;
        }
        for (MoveLine moveLine : moveLines) {
          bankReconciliationLine.getBankStatementLine().setMoveLine(moveLine);
          scriptContext =
              new Context(
                  Mapper.toMap(bankReconciliationLine.getBankStatementLine()),
                  BankStatementLineAFB120.class);
          String query =
              computeQuery(bankStatementQuery, dateMargin, amountMarginLow, amountMarginHigh);
          if (Boolean.TRUE.equals(new GroovyScriptHelper(scriptContext).eval(query))) {
            bankReconciliationLine =
                updateBankReconciliationLine(bankReconciliationLine, moveLine, bankStatementQuery);
            moveLine.setPostedNbr(bankReconciliationLine.getPostedNbr());
            moveLines.remove(moveLine);
            break;
          }
          bankReconciliationLine.getBankStatementLine().setMoveLine(null);
        }
        if (bankReconciliationLine.getMoveLine() != null) {
          continue;
        }
      }
    }
    return bankReconciliation;
  }

  protected BankReconciliationLine updateBankReconciliationLine(
      BankReconciliationLine bankReconciliationLine,
      MoveLine moveLine,
      BankStatementQuery bankStatementQuery) {
    bankReconciliationLine.setMoveLine(moveLine);
    bankReconciliationLine.setBankStatementQuery(bankStatementQuery);
    bankReconciliationLine.setConfidenceIndex(bankStatementQuery.getConfidenceIndex());
    bankReconciliationLine.setPostedNbr(bankReconciliationLine.getId().toString());
    return bankReconciliationLine;
  }

  protected String computeQuery(
      BankStatementQuery bankStatementQuery,
      BigInteger dateMargin,
      BigDecimal amountMarginLow,
      BigDecimal amountMarginHigh) {
    String query = bankStatementQuery.getQuery();
    query = query.replace("%amt+", amountMarginHigh.toString());
    query = query.replace("%amt-", amountMarginLow.toString());
    query = query.replace("%date", dateMargin.toString());
    return query;
  }

  public void unreconcileLines(List<BankReconciliationLine> bankReconciliationLines) {
    for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
      if (StringUtils.notEmpty((bankReconciliationLine.getPostedNbr()))) {
        unreconcileLine(bankReconciliationLine);
      }
    }
  }

  @Transactional
  public void unreconcileLine(BankReconciliationLine bankReconciliationLine) {
    bankReconciliationLine.setBankStatementQuery(null);
    bankReconciliationLine.setIsSelectedBankReconciliation(false);
    bankReconciliationLine.getBankStatementLine().setMoveLine(null);
    String query = "self.postedNbr LIKE '%%s%'";
    query = query.replace("%s", bankReconciliationLine.getPostedNbr());
    List<MoveLine> moveLines = moveLineRepository.all().filter(query).fetch();
    for (MoveLine moveLine : moveLines) {
      moveLine = moveLineService.removePostedNbr(moveLine, bankReconciliationLine.getPostedNbr());
    }
    bankReconciliationLine.setMoveLine(null);
    bankReconciliationLine.setConfidenceIndex(0);
    bankReconciliationLine.setPostedNbr("");
  }

  @Transactional
  public BankReconciliation computeInitialBalance(BankReconciliation bankReconciliation) {
    BankDetails bankDetails = bankReconciliation.getBankDetails();
    BankReconciliation previousBankReconciliation =
        bankReconciliationRepository
            .all()
            .filter("self.bankDetails = :bankDetails AND self.id != :id")
            .bind("bankDetails", bankDetails)
            .bind("id", bankReconciliation.getId())
            .order("-id")
            .fetchOne();
    BigDecimal startingBalance = BigDecimal.ZERO;
    if (ObjectUtils.isEmpty(previousBankReconciliation)) {
      BankStatementLine initialsBankStatementLine =
          bankStatementLineAFB120Repository
              .all()
              .filter(
                  "self.bankStatement = :bankStatement AND self.lineTypeSelect = :lineTypeSelect "
                      + "AND self.bankDetails = :bankDetails")
              .bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
              .bind("bankStatement", bankReconciliation.getBankStatement())
              .bind("bankDetails", bankDetails)
              .order("sequence")
              .fetchOne();
      startingBalance =
          initialsBankStatementLine.getCredit().subtract(initialsBankStatementLine.getDebit());
    } else {
      if (previousBankReconciliation.getStatusSelect()
          == BankReconciliationRepository.STATUS_VALIDATED) {
        startingBalance = previousBankReconciliation.getEndingBalance();
      } else {
        return null;
      }
    }
    bankReconciliation.setStartingBalance(startingBalance);
    return bankReconciliation;
  }

  public BankReconciliation computeEndingBalance(BankReconciliation bankReconciliation) {
    BigDecimal endingBalance = BigDecimal.ZERO;
    BigDecimal amount = BigDecimal.ZERO;
    endingBalance = endingBalance.add(bankReconciliation.getStartingBalance());
    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {
      amount = BigDecimal.ZERO;
      if (bankReconciliationLine.getMoveLine() != null) {
        amount =
            bankReconciliationLine
                .getMoveLine()
                .getDebit()
                .subtract(bankReconciliationLine.getMoveLine().getCredit());
      }
      endingBalance = endingBalance.add(amount);
    }
    bankReconciliation.setEndingBalance(endingBalance);
    return bankReconciliation;
  }

  public String printNewBankReconciliation(BankReconciliation bankReconciliation)
      throws AxelorException {
    if (bankReconciliation.getCompany() == null) {
      return null;
    }
    PrintingSettings printingSettings = bankReconciliation.getCompany().getPrintingSettings();
    String watermark = null;
    String fileLink = null;
    if (accountConfigRepository.findByCompany(bankReconciliation.getCompany()).getInvoiceWatermark()
        != null) {
      watermark =
          MetaFiles.getPath(
                  accountConfigRepository
                      .findByCompany(bankReconciliation.getCompany())
                      .getInvoiceWatermark())
              .toString();
    }
    fileLink =
        ReportFactory.createReport(
                IReport.BANK_RECONCILIATION2, I18n.get("Bank Reconciliation") + "-${date}")
            .addParam("BankReconciliationId", bankReconciliation.getId())
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .addParam(
                "Timezone",
                bankReconciliation.getCompany() != null
                    ? bankReconciliation.getCompany().getTimezone()
                    : null)
            .addParam("HeaderHeight", printingSettings.getPdfHeaderHeight())
            .addParam("Watermark", watermark)
            .addParam("FooterHeight", printingSettings.getPdfFooterHeight())
            .addFormat("pdf")
            .toAttach(bankReconciliation)
            .generate()
            .getFileLink();
    return fileLink;
  }

  @Transactional
  public BankReconciliationLine setSelected(BankReconciliationLine bankReconciliationLineContext) {
    BankReconciliationLine bankReconciliationLine =
        bankReconciliationLineRepository.find(bankReconciliationLineContext.getId());
    if (bankReconciliationLine.getIsSelectedBankReconciliation() != null) {
      bankReconciliationLine.setIsSelectedBankReconciliation(
          !bankReconciliationLineContext.getIsSelectedBankReconciliation());
    } else {
      bankReconciliationLine.setIsSelectedBankReconciliation(true);
    }
    return bankReconciliationLineRepository.save(bankReconciliationLine);
  }

  public String createDomainForMoveLine(BankReconciliation bankReconciliation)
      throws AxelorException {
    String domain = "";
    List<MoveLine> authorizedMoveLines =
        moveLineRepository
            .all()
            .filter(getRequestMoveLines(bankReconciliation))
            .bind(getBindRequestMoveLine(bankReconciliation))
            .fetch();

    String idList = StringTool.getIdListString(authorizedMoveLines);
    if (idList.equals("")) {
      domain = "self.id IN (0)";
    } else {
      domain = "self.id IN (" + idList + ")";
    }
    return domain;
  }

  public BankReconciliation onChangeBankStatement(BankReconciliation bankReconciliation)
      throws AxelorException {
    boolean uniqueBankDetails = true;
    BankDetails bankDetails = null;
    bankReconciliation.setToDate(bankReconciliation.getBankStatement().getToDate());
    bankReconciliation.setFromDate(bankReconciliation.getBankStatement().getFromDate());
    List<BankStatementLine> bankStatementLines =
        bankStatementLineRepository
            .findByBankStatement(bankReconciliation.getBankStatement())
            .fetch();
    for (BankStatementLine bankStatementLine : bankStatementLines) {
      if (bankDetails == null) {
        bankDetails = bankStatementLine.getBankDetails();
      }
      // If it is still null
      if (bankDetails == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.BANK_RECONCILIATION_BANK_STATEMENT_NO_BANK_DETAIL));
      }
      if (!bankDetails.equals(bankStatementLine.getBankDetails())) {
        uniqueBankDetails = false;
      }
    }
    if (uniqueBankDetails) {
      bankReconciliation.setBankDetails(bankDetails);
      bankReconciliation.setCashAccount(bankDetails.getBankAccount());
      bankReconciliation.setJournal(bankDetails.getJournal());
    } else {
      bankReconciliation.setBankDetails(null);
    }
    return bankReconciliation;
  }

  public void checkReconciliation(List<MoveLine> moveLines, BankReconciliation br)
      throws AxelorException {

    if (br.getBankReconciliationLineList().stream()
                .filter(line -> line.getIsSelectedBankReconciliation())
                .count()
            == 0
        || moveLines.size() == 0) {
      if (br.getBankReconciliationLineList().stream()
                  .filter(line -> line.getIsSelectedBankReconciliation())
                  .count()
              == 0
          && moveLines.size() == 0) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                IExceptionMessage
                    .BANK_RECONCILIATION_SELECT_MOVE_LINE_AND_BANK_RECONCILIATION_LINE));
      } else if (br.getBankReconciliationLineList().stream()
              .filter(line -> line.getIsSelectedBankReconciliation())
              .count()
          == 0) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.BANK_RECONCILIATION_SELECT_BANK_RECONCILIATION_LINE));
      } else if (moveLines.size() == 0) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.BANK_RECONCILIATION_SELECT_MOVE_LINE));
      }
    } else if (br.getBankReconciliationLineList().stream()
                .filter(line -> line.getIsSelectedBankReconciliation())
                .count()
            > 1
        || moveLines.size() > 1) {
      if (br.getBankReconciliationLineList().stream()
                  .filter(line -> line.getIsSelectedBankReconciliation())
                  .count()
              > 1
          && moveLines.size() > 1) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(
                IExceptionMessage
                    .BANK_RECONCILIATION_SELECT_MOVE_LINE_AND_BANK_RECONCILIATION_LINE));
      } else if (br.getBankReconciliationLineList().stream()
              .filter(line -> line.getIsSelectedBankReconciliation())
              .count()
          > 1) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.BANK_RECONCILIATION_SELECT_BANK_RECONCILIATION_LINE));
      } else if (moveLines.size() > 1) {
        throw new AxelorException(
            br,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.BANK_RECONCILIATION_SELECT_MOVE_LINE));
      }
    }
  }

  public void reconcileSelected(BankReconciliation bankReconciliation) throws AxelorException {
    BankReconciliationLine bankReconciliationLine;
    String filter = getRequestMoveLines(bankReconciliation);
    filter = filter.concat(" AND self.isSelectedBankReconciliation = true");
    List<MoveLine> moveLines =
        moveLineRepository
            .all()
            .filter(filter)
            .bind(getBindRequestMoveLine(bankReconciliation))
            .fetch();
    checkReconciliation(moveLines, bankReconciliation);
    bankReconciliationLine =
        bankReconciliation.getBankReconciliationLineList().stream()
            .filter(line -> line.getIsSelectedBankReconciliation())
            .collect(Collectors.toList())
            .get(0);
    bankReconciliationLine.setMoveLine(moveLines.get(0));
    bankReconciliationLine =
        bankReconciliationLineService.reconcileBRLAndMoveLine(
            bankReconciliationLine, moveLines.get(0));
  }

  public String getDomainForWizard(
      BankReconciliation bankReconciliation,
      BigDecimal bankStatementCredit,
      BigDecimal bankStatementDebit) {
    if (bankReconciliation != null
        && bankReconciliation.getCompany() != null
        && bankStatementCredit != null
        && bankStatementDebit != null) {
      String query =
          "(self.bankReconciledAmount < self.debit or self.bankReconciledAmount < self.credit)"
              + " AND self.move.company.id = "
              + bankReconciliation.getCompany().getId()
              + " AND (self.move.statusSelect = "
              + MoveRepository.STATUS_ACCOUNTED
              + " OR self.move.statusSelect = "
              + MoveRepository.STATUS_VALIDATED
              + ")";
      if (bankStatementCredit.signum() > 0) {
        query = query.concat(" AND self.debit > 0");
      }
      if (bankStatementDebit.signum() > 0) {
        query = query.concat(" AND self.credit > 0");
      }
      if (bankReconciliation.getCashAccount() != null) {
        query =
            query.concat(" AND self.account.id = " + bankReconciliation.getCashAccount().getId());
      } else {
        query =
            query.concat(
                " AND self.account.accountType.technicalTypeSelect LIKE '"
                    + AccountTypeRepository.TYPE_CASH
                    + "'");
      }
      if (bankReconciliation.getJournal() != null) {
        query =
            query.concat(" AND self.move.journal.id = " + bankReconciliation.getJournal().getId());
      } else {
        query =
            query.concat(
                " AND self.move.journal.journalType.technicalTypeSelect = "
                    + JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY);
      }
      return query;
    }
    return "self id in (0)";
  }

  public BigDecimal getSelectedMoveLineTotal(List<LinkedHashMap> toReconcileMoveLineSet) {
    BigDecimal selectedMoveLineTotal = BigDecimal.ZERO;
    List<MoveLine> moveLineList = new ArrayList<>();
    toReconcileMoveLineSet.forEach(
        m ->
            moveLineList.add(
                moveLineRepository.find(
                    Long.valueOf((Integer) ((LinkedHashMap<?, ?>) m).get("id")))));
    for (MoveLine moveLine : moveLineList) {
      selectedMoveLineTotal =
          selectedMoveLineTotal.add(moveLine.getDebit().add(moveLine.getCredit()));
    }
    return selectedMoveLineTotal;
  }
}
