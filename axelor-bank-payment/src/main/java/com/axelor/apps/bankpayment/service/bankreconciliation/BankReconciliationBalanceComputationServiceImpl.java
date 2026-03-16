/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.db.JPA;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class BankReconciliationBalanceComputationServiceImpl
    implements BankReconciliationBalanceComputationService {

  protected BankReconciliationRepository bankReconciliationRepository;
  protected AccountService accountService;
  protected MoveLineRepository moveLineRepository;
  protected BankReconciliationComputeService bankReconciliationComputeService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BankReconciliationBalanceComputationServiceImpl(
      BankReconciliationRepository bankReconciliationRepository,
      AccountService accountService,
      MoveLineRepository moveLineRepository,
      BankReconciliationComputeService bankReconciliationComputeService,
      CurrencyScaleService currencyScaleService) {
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.accountService = accountService;
    this.moveLineRepository = moveLineRepository;
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

    BigDecimal statementReconciledLineBalance = BigDecimal.ZERO;
    BigDecimal movesReconciledLineBalance = BigDecimal.ZERO;
    BigDecimal statementUnreconciledLineBalance = BigDecimal.ZERO;
    BigDecimal movesUnreconciledLineBalance = BigDecimal.ZERO;
    BigDecimal statementOngoingReconciledBalance = BigDecimal.ZERO;
    BigDecimal movesOngoingReconciledBalance = BigDecimal.ZERO;

    boolean isMultiCurrency = isMultiCurrency(bankReconciliation);
    Currency bankReconciliationCurrency = bankReconciliation.getCurrency();

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
                  movesOngoingReconciledBalance,
                  brl,
                  moveLineSet,
                  isMultiCurrency,
                  bankReconciliationCurrency);
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
    JPA.clear();

    bankReconciliation = bankReconciliationRepository.find(bankReconciliation.getId());
    Pair<BigDecimal, BigDecimal> balances = computeBalances(bankReconciliation, isMultiCurrency);
    movesReconciledLineBalance = movesReconciledLineBalance.add(balances.getLeft());
    movesUnreconciledLineBalance = movesUnreconciledLineBalance.add(balances.getRight());

    Account cashAccount = bankReconciliation.getCashAccount();
    if (cashAccount != null) {
      bankReconciliation.setAccountBalance(
          accountService.computeBalance(cashAccount, AccountService.BALANCE_TYPE_DEBIT_BALANCE));
    }

    bankReconciliation.setStatementReconciledLineBalance(
        currencyScaleService.getScaledValue(bankReconciliation, statementReconciledLineBalance));
    bankReconciliation.setMovesReconciledLineBalance(
        currencyScaleService.getScaledValue(bankReconciliation, movesReconciledLineBalance));
    bankReconciliation.setStatementUnreconciledLineBalance(
        currencyScaleService.getScaledValue(bankReconciliation, statementUnreconciledLineBalance));
    bankReconciliation.setMovesUnreconciledLineBalance(
        currencyScaleService.getScaledValue(bankReconciliation, movesUnreconciledLineBalance));
    bankReconciliation.setStatementOngoingReconciledBalance(
        currencyScaleService.getScaledValue(bankReconciliation, statementOngoingReconciledBalance));
    bankReconciliation.setMovesOngoingReconciledBalance(
        currencyScaleService.getScaledValue(bankReconciliation, movesOngoingReconciledBalance));
    bankReconciliation.setStatementAmountRemainingToReconcile(
        currencyScaleService.getScaledValue(
            bankReconciliation,
            statementUnreconciledLineBalance.subtract(statementOngoingReconciledBalance)));
    bankReconciliation.setMovesAmountRemainingToReconcile(
        currencyScaleService.getScaledValue(
            bankReconciliation,
            movesUnreconciledLineBalance.subtract(movesOngoingReconciledBalance)));
    bankReconciliation.setStatementTheoreticalBalance(
        currencyScaleService.getScaledValue(
            bankReconciliation,
            statementReconciledLineBalance.add(statementOngoingReconciledBalance)));
    bankReconciliation.setMovesTheoreticalBalance(
        currencyScaleService.getScaledValue(
            bankReconciliation, movesReconciledLineBalance.add(movesOngoingReconciledBalance)));
    bankReconciliation = bankReconciliationComputeService.computeEndingBalance(bankReconciliation);
    return bankReconciliationRepository.save(bankReconciliation);
  }

  public Pair<BigDecimal, BigDecimal> computeBalances(
      BankReconciliation bankReconciliation, boolean isMultiCurrency) {

    Account cashAccount = bankReconciliation.getCashAccount();

    String amountQuery;
    if (isMultiCurrency) {
      amountQuery =
          "CASE WHEN ml.move.currency = :bankReconciliationCurrency"
              + " THEN abs(ml.currencyAmount)"
              + " ELSE (ml.debit + ml.credit) END";
    } else {
      amountQuery = "(ml.debit + ml.credit)";
    }

    String balanceQuery =
        "SELECT "
            + "SUM(FUNCTION('ROUND', "
            + "    CASE WHEN ml.debit <> 0 THEN ml.bankReconciledAmount "
            + "         ELSE -ml.bankReconciledAmount END, c.numberOfDecimals"
            + ")), "
            + "SUM(FUNCTION('ROUND', "
            + "    CASE WHEN ml.debit <> 0 THEN ("
            + amountQuery
            + " - ml.bankReconciledAmount) "
            + "         ELSE -("
            + amountQuery
            + " - ml.bankReconciledAmount) END, c.numberOfDecimals"
            + ")) "
            + "FROM MoveLine ml "
            + "JOIN ml.move.companyCurrency c "
            + "WHERE ml.account = :cashAccount "
            + "AND ml.move.statusSelect IN (:daybook, :accounted)";

    Query query =
        JPA.em()
            .createQuery(balanceQuery)
            .setParameter("cashAccount", cashAccount)
            .setParameter("daybook", MoveRepository.STATUS_DAYBOOK)
            .setParameter("accounted", MoveRepository.STATUS_ACCOUNTED);

    if (isMultiCurrency) {
      query.setParameter("bankReconciliationCurrency", bankReconciliation.getCurrency());
    }

    Object[] results = (Object[]) query.getSingleResult();

    BigDecimal reconciled = results[0] == null ? BigDecimal.ZERO : (BigDecimal) results[0];
    BigDecimal unreconciled = results[1] == null ? BigDecimal.ZERO : (BigDecimal) results[1];

    return Pair.of(reconciled, unreconciled);
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
      Set<MoveLine> moveLineSet,
      boolean isMultiCurrency,
      Currency bankReconciliationCurrency) {
    if (!brl.getIsPosted() && !Strings.isNullOrEmpty(brl.getPostedNbr())) {
      String query = "self.postedNbr LIKE '%%s%'";
      query = query.replace("%s", brl.getPostedNbr());
      query += " AND self.move.company = :company";

      List<MoveLine> moveLines =
          moveLineRepository
              .all()
              .filter(query)
              .bind("company", brl.getBankReconciliation().getCompany())
              .fetch();
      for (MoveLine moveLine : moveLines) {
        // To avoid the fact that a moveline can be related to multiple brl and so, the update of
        // the amount can be duplicated
        if (moveLineSet.contains(moveLine)) {
          continue;
        } else {
          moveLineSet.add(moveLine);
        }

        boolean isCurrencyAmount =
            isMultiCurrency
                && Optional.ofNullable(moveLine.getMove())
                    .map(move -> move.getCurrency())
                    .map(currency -> currency.equals(bankReconciliationCurrency))
                    .orElse(false);
        BigDecimal amount =
            isCurrencyAmount
                ? moveLine.getCurrencyAmount().abs()
                : moveLine.getCredit().add(moveLine.getDebit());

        if (moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0) {
          movesOngoingReconciledBalance = movesOngoingReconciledBalance.add(amount);
        } else {
          movesOngoingReconciledBalance = movesOngoingReconciledBalance.subtract(amount);
        }
      }
    }
    return currencyScaleService.getScaledValue(brl, movesOngoingReconciledBalance);
  }

  protected boolean isMultiCurrency(BankReconciliation bankReconciliation) {
    Currency currency = bankReconciliation.getCurrency();
    Currency companyCurrency =
        Optional.ofNullable(bankReconciliation.getCompany()).map(Company::getCurrency).orElse(null);
    return currency != null && companyCurrency != null && !currency.equals(companyCurrency);
  }
}
