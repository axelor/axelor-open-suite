/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountManagementRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementQueryRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRuleRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.BankReconciliationLoadService;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.afb120.BankReconciliationLoadAFB120Service;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BankReconciliationService {

  protected AccountManagementRepository accountManagementRepository;
  protected AccountService accountService;
  protected BankReconciliationRepository bankReconciliationRepository;
  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;
  protected BankStatementQueryRepository bankStatementQueryRepository;
  protected MoveLineRepository moveLineRepository;

  @Inject
  public BankReconciliationService(
      BankReconciliationRepository bankReconciliationRepository,
      AccountService accountService,
      AccountManagementRepository accountManagementRepository,
      BankStatementQueryRepository bankStatementQueryRepository,
      MoveLineRepository moveLineRepository,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository) {

    this.bankReconciliationRepository = bankReconciliationRepository;
    this.accountService = accountService;
    this.accountManagementRepository = accountManagementRepository;
    this.bankStatementQueryRepository = bankStatementQueryRepository;
    this.moveLineRepository = moveLineRepository;
    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
  }

  @Transactional
  public void compute(BankReconciliation bankReconciliation) {
    int limit = 10;
    int offset = 0;
    List<BankReconciliation> bankReconciliations;
    List<MoveLine> moveLines;

    BigDecimal totalPaid = BigDecimal.ZERO;
    BigDecimal totalCashed = BigDecimal.ZERO;

    /* DONE
     * Charger tous les rapprochements, ajouter le débit de toutes bankReconciliationLine where isPosted = true;
     * soustraire le crédit de ces même lignes
     */
    BigDecimal statementReconciledLineBalance = BigDecimal.ZERO;
    /*
     * Charger tous les moveLines where account = bankReconciliation.cashAccount (if bankReonciliation.cashAccount == null, on compte 0)
     */
    BigDecimal movesReconciledLineBalance = BigDecimal.ZERO;
    /* DONE
     * Charger tous les rapprochements, ajouter le débit de toutes bankReconciliationLine where isPosted = false;
     * soustraire le crédit de ces même lignes
     */
    BigDecimal statementUnreconciledLineBalance = BigDecimal.ZERO;
    /*
     * Charger les movelines dont le montant rapproché est différent du crédit ou débit en fonction, et ajouter / soustraire la différence
     */
    BigDecimal movesUnreconciledLineBalance = BigDecimal.ZERO;
    /*
     * Charger tous les rapprochements, ajouter le débit de toutes bankReconciliationLine where isPosted = false; and postedNumber != 0
     */
    BigDecimal statementOngoingReconciledBalance = BigDecimal.ZERO;
    BigDecimal movesOngoingReconciledBalance = BigDecimal.ZERO;

    bankReconciliations = bankReconciliationRepository.all().fetch(limit, offset);
    do {
      for (BankReconciliation br : bankReconciliations) {
        for (BankReconciliationLine brl : br.getBankReconciliationLineList()) {
          if (brl.getIsPosted()) {
            statementReconciledLineBalance = statementReconciledLineBalance.add(brl.getDebit());
            statementReconciledLineBalance =
                statementReconciledLineBalance.subtract(brl.getCredit());
          } else {
            statementUnreconciledLineBalance = statementUnreconciledLineBalance.add(brl.getDebit());
            statementUnreconciledLineBalance =
                statementUnreconciledLineBalance.subtract(brl.getCredit());
          }
        }
      }
      offset += limit;
      JPA.clear();
      bankReconciliations = bankReconciliationRepository.all().fetch(limit, offset);
    } while (bankReconciliations.size() != 0);

    offset = 0;
    moveLines =
        moveLineRepository
            .all()
            .filter("self.account = :cashAccount")
            .bind("cashAccount", bankReconciliation.getCashAccount())
            .fetch(limit, offset);
    do {
    	for(MoveLine moveLine : moveLines)
    	{
    		if(moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0)
    		{// Debit line
    			
    		}

    		if(moveLine.getCredit().compareTo(BigDecimal.ZERO) != 0)
    		{// Credit line
    			
    		}
    		
    	}
        offset += limit;
        JPA.clear();
        moveLines =
                moveLineRepository
                    .all()
                    .filter("self.account = :cashAccount")
                    .bind("cashAccount", bankReconciliation.getCashAccount())
                    .fetch(limit, offset);
    } while (moveLines.size() != 0);

    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {
      totalPaid = totalPaid.add(bankReconciliationLine.getDebit());
      totalCashed = totalCashed.add(bankReconciliationLine.getCredit());
    }

    bankReconciliation.setTotalPaid(totalPaid);
    bankReconciliation.setTotalCashed(totalCashed);
    Account cashAccount = bankReconciliation.getCashAccount();
    if (cashAccount != null) {
      bankReconciliation.setAccountBalance(
          accountService.computeBalance(cashAccount, AccountService.BALANCE_TYPE_DEBIT_BALANCE));
    }
    bankReconciliation.setComputedBalance(
        bankReconciliation.getAccountBalance().add(totalCashed).subtract(totalPaid));
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
  }

  public String createDomainForBankDetails(BankReconciliation bankReconciliation) {

    return Beans.get(BankDetailsService.class)
        .getActiveCompanyBankDetails(
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
        Beans.get(BankReconciliationLoadAFB120Service.class)
            .loadBankStatement(bankReconciliation, includeBankStatement);
        break;

      default:
        Beans.get(BankReconciliationLoadService.class)
            .loadBankStatement(bankReconciliation, includeBankStatement);
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
      journal = Beans.get(JournalRepository.class).find(Long.parseLong(journalIds));
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
      cashAccount = Beans.get(AccountRepository.class).find(Long.parseLong(cashAccountIds));
    }
    return cashAccount;
  }

  public void reconciliateAccordingToQueries(BankReconciliation bankReconciliation) {
    List<BankStatementQuery> bankStatementQueries =
        bankStatementQueryRepository
            .findByRuleType(BankStatementRuleRepository.RULE_TYPE_RECONCILIATION_AUTO)
            .fetch();
    List<BankReconciliationLine> bankReconciliationLines =
        bankReconciliation.getBankReconciliationLineList();
    List<MoveLine> moveLines =
        moveLineRepository
            .all()
            .filter(
                "self.move.journal.id = :journal AND self.account = :cashAccount AND self.move.statusSelect != :statusSelect AND ((self.debit > 0 AND self.bankReconciledAmount < self.debit) OR (self.credit > 0 AND self.bankReconciledAmount < self.credit))")
            .bind("statusSelect", MoveRepository.STATUS_CANCELED)
            .bind("journal", bankReconciliation.getJournal())
            .bind("cashAccount", bankReconciliation.getCashAccount())
            .fetch();
    bankReconciliationLines =
        bankReconciliationLines.stream()
            .filter(line -> line.getMoveLine() == null)
            .collect(Collectors.toList());
    Context scriptContext;
    for (BankStatementQuery query : bankStatementQueries) {
      for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
        if (bankReconciliationLine.getMoveLine() != null) continue;
        for (MoveLine moveLine : moveLines) {
          bankReconciliationLine.setMoveLine(moveLine);
          scriptContext =
              new Context(
                  Mapper.toMap(bankReconciliationLine), BankStatementLineAFB120.class.getClass());
          if (Boolean.TRUE.equals(new GroovyScriptHelper(scriptContext).eval(query.getQuery()))) {
            bankReconciliationLine.setBankStatementQuery(query);
            bankReconciliationLine.setConfidenceIndex(query.getConfidenceIndex());
            bankReconciliationLine.setPostedNbr(bankReconciliationLine.getId());
            moveLine.setPostedNbr(bankReconciliationLine.getPostedNbr());
            moveLines.remove(moveLine);
            break;
          } else {
            bankReconciliationLine.setMoveLine(null);
          }
        }
        if (bankReconciliationLine.getMoveLine() != null) break;
      }
    }
  }

  @Transactional
  public void unreconcileLines(List<BankReconciliationLine> bankReconciliationLines) {
    for (BankReconciliationLine bankReconcileLine : bankReconciliationLines) {
      bankReconcileLine.getMoveLine().setPostedNbr(0L);
      bankReconcileLine.setMoveLine(null);
    }
  }
}
