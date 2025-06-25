/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DateService;
import com.axelor.db.JPA;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BankReconciliationBalanceComputationServiceImpl
    implements BankReconciliationBalanceComputationService {

  protected BankReconciliationRepository bankReconciliationRepository;
  protected AccountService accountService;
  protected BankReconciliationLineRepository bankReconciliationLineRepository;
  protected MoveLineRepository moveLineRepository;
  protected CurrencyService currencyService;
  protected DateService dateService;
  protected BankReconciliationComputeService bankReconciliationComputeService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BankReconciliationBalanceComputationServiceImpl(
      BankReconciliationRepository bankReconciliationRepository,
      AccountService accountService,
      BankReconciliationLineRepository bankReconciliationLineRepository,
      MoveLineRepository moveLineRepository,
      CurrencyService currencyService,
      DateService dateService,
      BankReconciliationComputeService bankReconciliationComputeService,
      CurrencyScaleService currencyScaleService) {
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.accountService = accountService;
    this.bankReconciliationLineRepository = bankReconciliationLineRepository;
    this.moveLineRepository = moveLineRepository;
    this.currencyService = currencyService;
    this.dateService = dateService;
    this.bankReconciliationComputeService = bankReconciliationComputeService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  @Transactional
  public BankReconciliation computeBalances(BankReconciliation bankReconciliation)
      throws AxelorException {
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
          currencyScaleService.getScaledValue(
              bankReconciliation,
              statementReconciledLineBalance.add(bankReconciliations.get(0).getStartingBalance()));
      movesReconciledLineBalance =
          currencyScaleService.getScaledValue(
              bankReconciliation,
              movesReconciledLineBalance.add(bankReconciliations.get(0).getStartingBalance()));
    }
    do {
      for (BankReconciliation br : bankReconciliations) {
        Set<MoveLine> moveLineSet = new HashSet<>();
        for (BankReconciliationLine brl : br.getBankReconciliationLineList()) {
          statementReconciledLineBalance =
              computeStatementReconciledLineBalance(statementReconciledLineBalance, brl);
          statementUnreconciledLineBalance =
              computeStatementUnreconciledLineBalance(statementUnreconciledLineBalance, brl);
          statementOngoingReconciledBalance =
              computeStatementOngoingReconciledLineBalance(statementOngoingReconciledBalance, brl);
          movesOngoingReconciledBalance =
              computeMovesOngoingReconciledLineBalance(
                  movesOngoingReconciledBalance, brl, moveLineSet);
        }
      }
      offset += limit;
      JPA.clear();
      bankReconciliations =
          bankReconciliationRepository
              .findByBankDetails(bankReconciliation.getBankDetails())
              .order("id")
              .fetch(limit, offset);
    } while (bankReconciliations.size() != 0);

    offset = 0;
    JPA.clear();
    bankReconciliation = bankReconciliationRepository.find(bankReconciliation.getId());
    moveLines = this.getMoveLines(bankReconciliation.getCashAccount(), limit, offset);

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
      moveLines = this.getMoveLines(bankReconciliation.getCashAccount(), limit, offset);

    } while (moveLines.size() != 0);
    JPA.clear();
    bankReconciliation = bankReconciliationRepository.find(bankReconciliation.getId());
    Account cashAccount = bankReconciliation.getCashAccount();
    if (cashAccount != null) {
      bankReconciliation.setAccountBalance(
          accountService.computeBalance(cashAccount, AccountService.BALANCE_TYPE_DEBIT_BALANCE));
    }

    bankReconciliation.setStatementReconciledLineBalance(
        getConvertedAmount(statementReconciledLineBalance, bankReconciliation));
    bankReconciliation.setMovesReconciledLineBalance(
        getConvertedAmount(movesReconciledLineBalance, bankReconciliation));
    bankReconciliation.setStatementUnreconciledLineBalance(
        getConvertedAmount(statementUnreconciledLineBalance, bankReconciliation));
    bankReconciliation.setMovesUnreconciledLineBalance(
        getConvertedAmount(movesUnreconciledLineBalance, bankReconciliation));
    bankReconciliation.setStatementOngoingReconciledBalance(
        getConvertedAmount(statementOngoingReconciledBalance, bankReconciliation));
    bankReconciliation.setMovesOngoingReconciledBalance(
        getConvertedAmount(movesOngoingReconciledBalance, bankReconciliation));
    bankReconciliation.setStatementAmountRemainingToReconcile(
        getConvertedAmount(
            statementUnreconciledLineBalance.subtract(statementOngoingReconciledBalance),
            bankReconciliation));
    bankReconciliation.setMovesAmountRemainingToReconcile(
        getConvertedAmount(
            movesUnreconciledLineBalance.subtract(movesOngoingReconciledBalance),
            bankReconciliation));
    bankReconciliation.setStatementTheoreticalBalance(
        getConvertedAmount(
            statementReconciledLineBalance.add(statementOngoingReconciledBalance),
            bankReconciliation));
    bankReconciliation.setMovesTheoreticalBalance(
        getConvertedAmount(
            movesReconciledLineBalance.add(movesOngoingReconciledBalance), bankReconciliation));
    bankReconciliation = bankReconciliationComputeService.computeEndingBalance(bankReconciliation);
    return bankReconciliationRepository.save(bankReconciliation);
  }

  protected List<MoveLine> getMoveLines(Account cashAccount, int limit, int offset) {
    return moveLineRepository
        .all()
        .filter("self.account = :cashAccount AND self.move.statusSelect IN (:daybook, :accounted)")
        .bind("cashAccount", cashAccount)
        .bind("daybook", MoveRepository.STATUS_DAYBOOK)
        .bind("accounted", MoveRepository.STATUS_ACCOUNTED)
        .order("id")
        .fetch(limit, offset);
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
    return currencyScaleService.getCompanyScaledValue(moveLine, movesReconciledLineBalance);
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
    return currencyScaleService.getCompanyScaledValue(moveLine, movesUnreconciledLineBalance);
  }

  protected BigDecimal computeStatementReconciledLineBalance(
      BigDecimal statementReconciledLineBalance, BankReconciliationLine brl) {
    if (brl.getIsPosted()) {

      statementReconciledLineBalance = statementReconciledLineBalance.subtract(brl.getDebit());
      statementReconciledLineBalance = statementReconciledLineBalance.add(brl.getCredit());
    }
    return currencyScaleService.getScaledValue(brl, statementReconciledLineBalance);
  }

  protected BigDecimal computeStatementUnreconciledLineBalance(
      BigDecimal statementUnreconciledLineBalance, BankReconciliationLine brl) {
    if (!brl.getIsPosted()) {
      statementUnreconciledLineBalance = statementUnreconciledLineBalance.subtract(brl.getDebit());
      statementUnreconciledLineBalance = statementUnreconciledLineBalance.add(brl.getCredit());
    }
    return currencyScaleService.getScaledValue(brl, statementUnreconciledLineBalance);
  }

  protected BigDecimal computeStatementOngoingReconciledLineBalance(
      BigDecimal statementOngoingReconciledBalance, BankReconciliationLine brl) {
    if (!brl.getIsPosted() && !Strings.isNullOrEmpty(brl.getPostedNbr())) {
      statementOngoingReconciledBalance =
          statementOngoingReconciledBalance.subtract(brl.getDebit());
      statementOngoingReconciledBalance = statementOngoingReconciledBalance.add(brl.getCredit());
    }
    return currencyScaleService.getScaledValue(brl, statementOngoingReconciledBalance);
  }

  protected BigDecimal computeMovesOngoingReconciledLineBalance(
      BigDecimal movesOngoingReconciledBalance,
      BankReconciliationLine brl,
      Set<MoveLine> moveLineSet) {
    if (!brl.getIsPosted() && !Strings.isNullOrEmpty(brl.getPostedNbr())) {
      String query = "self.postedNbr LIKE '%%s%'";
      query = query.replace("%s", brl.getPostedNbr());
      List<MoveLine> moveLines = moveLineRepository.all().filter(query).fetch();
      for (MoveLine moveLine : moveLines) {
        // To avoid the fact that a moveline can be related to multiple brl and so, the update of
        // the amount can be duplicated
        if (moveLineSet.contains(moveLine)) {
          continue;
        } else {
          moveLineSet.add(moveLine);
        }

        if (moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0) {
          movesOngoingReconciledBalance =
              movesOngoingReconciledBalance.add(moveLine.getCredit().add(moveLine.getDebit()));
        } else {
          movesOngoingReconciledBalance =
              movesOngoingReconciledBalance.subtract(moveLine.getCredit().add(moveLine.getDebit()));
        }
      }
    }
    return currencyScaleService.getScaledValue(brl, movesOngoingReconciledBalance);
  }

  protected BigDecimal getConvertedAmount(BigDecimal value, BankReconciliation bankReconciliation)
      throws AxelorException {
    return currencyService.getAmountCurrencyConvertedAtDate(
        bankReconciliation.getCurrency(),
        bankReconciliation.getCompany().getCurrency(),
        value,
        dateService.date());
  }
}
