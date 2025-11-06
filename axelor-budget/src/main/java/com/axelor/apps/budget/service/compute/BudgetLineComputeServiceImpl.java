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
package com.axelor.apps.budget.service.compute;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.repo.BudgetLineRepository;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

public class BudgetLineComputeServiceImpl implements BudgetLineComputeService {

  protected CurrencyScaleService currencyScaleService;
  protected BudgetLineRepository budgetLineRepository;

  public static final BigDecimal gap = BigDecimal.valueOf(0.01);

  @Inject
  public BudgetLineComputeServiceImpl(
      CurrencyScaleService currencyScaleService, BudgetLineRepository budgetLineRepository) {
    this.currencyScaleService = currencyScaleService;
    this.budgetLineRepository = budgetLineRepository;
  }

  @FunctionalInterface
  protected interface ComputeMethod {
    void computeBudgetLineAmounts(BudgetLine budgetLine, BigDecimal amount);
  }

  @Override
  public void updateBudgetLineAmounts(
      Move move,
      Budget budget,
      BigDecimal amount,
      LocalDate fromDate,
      LocalDate toDate,
      LocalDate defaultDate) {
    ComputeMethod computeMethod = this::updateBudgetLineAmountsWithNoPo;
    Invoice invoice = Optional.ofNullable(move).map(Move::getInvoice).orElse(null);
    if (invoice != null && (invoice.getPurchaseOrder() != null || invoice.getSaleOrder() != null)) {
      computeMethod = this::updateBudgetLineAmountsWithPo;
    }

    updateBudgetLineAmounts(budget, amount, fromDate, toDate, defaultDate, computeMethod);
  }

  @Override
  public void updateBudgetLineAmountsOnOrder(
      BigDecimal amountInvoiced,
      Budget budget,
      BigDecimal amount,
      LocalDate fromDate,
      LocalDate toDate,
      LocalDate defaultDate) {
    if (amountInvoiced.signum() != 0) {
      return;
    }

    ComputeMethod computeMethod = this::updateBudgetLineAmountsOnOrder;

    updateBudgetLineAmounts(budget, amount, fromDate, toDate, defaultDate, computeMethod);
  }

  @Override
  public void updateBudgetLineAmountsPaid(
      Budget budget,
      BigDecimal amount,
      LocalDate fromDate,
      LocalDate toDate,
      LocalDate defaultDate) {
    ComputeMethod computeMethod = this::updateBudgetLineAmountPaid;

    updateBudgetLineAmounts(budget, amount, fromDate, toDate, defaultDate, computeMethod);
  }

  protected void updateBudgetLineAmounts(
      Budget budget,
      BigDecimal amount,
      LocalDate fromDate,
      LocalDate toDate,
      LocalDate defaultDate,
      ComputeMethod computeMethod) {
    if (budget == null || amount.signum() == 0 || computeMethod == null) {
      return;
    }

    if (fromDate != null && toDate != null) {
      computeBudgetLinesUsingDates(budget, amount, fromDate, toDate, computeMethod);
    } else {
      BudgetLine budgetLine = budgetLineRepository.findCurrentByDate(budget, defaultDate);
      if (budgetLine != null) {
        computeMethod.computeBudgetLineAmounts(budgetLine, amount);
      }
    }
  }

  protected void computeBudgetLinesUsingDates(
      Budget budget,
      BigDecimal amount,
      LocalDate fromDate,
      LocalDate toDate,
      ComputeMethod computeMethod) {
    long totalDuration = LocalDateHelper.daysBetween(fromDate, toDate, false);
    LocalDate date = fromDate;
    BigDecimal prorataAmount;
    BigDecimal missingAmount = amount;

    while (!date.isAfter(toDate)) {
      BudgetLine budgetLine = budgetLineRepository.findCurrentByDate(budget, date);
      if (budgetLine == null || totalDuration == 0) {
        break;
      }
      if (!toDate.isAfter(budgetLine.getToDate())) {
        prorataAmount = missingAmount;
      } else {
        prorataAmount =
            computeProrataAmountOnDates(
                amount, date, budgetLine.getToDate(), missingAmount, totalDuration);
        missingAmount = missingAmount.subtract(prorataAmount);
      }
      computeMethod.computeBudgetLineAmounts(budgetLine, prorataAmount);
      date = budgetLine.getToDate().plusDays(1);
    }
  }

  protected BigDecimal computeProrataAmountOnDates(
      BigDecimal amount,
      LocalDate fromDate,
      LocalDate toDate,
      BigDecimal missingAmount,
      long totalDuration) {
    if (totalDuration == 0 || fromDate == null || toDate == null || amount.signum() == 0) {
      return BigDecimal.ZERO;
    }

    long duration = LocalDateHelper.daysBetween(fromDate, toDate, false);
    BigDecimal prorataAmount =
        amount.multiply(
            BigDecimal.valueOf(duration)
                .divide(
                    BigDecimal.valueOf(totalDuration),
                    AppBaseService.COMPUTATION_SCALING,
                    RoundingMode.HALF_UP));
    prorataAmount =
        prorataAmount.setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

    if (missingAmount.subtract(prorataAmount).compareTo(gap) == 0) {
      prorataAmount = prorataAmount.add(gap);
    }

    return prorataAmount;
  }

  protected void updateBudgetLineAmountsOnOrder(BudgetLine budgetLine, BigDecimal amount) {
    Budget budget = budgetLine.getBudget();
    budgetLine.setAmountCommitted(
        currencyScaleService.getCompanyScaledValue(
            budget, budgetLine.getAmountCommitted().add(amount)));
    budgetLine.setToBeCommittedAmount(
        currencyScaleService.getCompanyScaledValue(
            budget, budgetLine.getAmountExpected().subtract(budgetLine.getAmountCommitted())));
  }

  protected void updateBudgetLineAmountPaid(BudgetLine budgetLine, BigDecimal amount) {
    Budget budget = budgetLine.getBudget();
    budgetLine.setAmountPaid(
        currencyScaleService.getCompanyScaledValue(budget, budgetLine.getAmountPaid().add(amount)));
  }

  protected void updateBudgetLineAmountsWithNoPo(BudgetLine budgetLine, BigDecimal amount) {
    Budget budget = budgetLine.getBudget();
    budgetLine.setRealizedWithNoPo(
        currencyScaleService.getCompanyScaledValue(
            budget, budgetLine.getRealizedWithNoPo().add(amount)));
    computeOtherFields(budget, budgetLine, amount);
  }

  protected void updateBudgetLineAmountsWithPo(BudgetLine budgetLine, BigDecimal amount) {
    Budget budget = budgetLine.getBudget();
    budgetLine.setRealizedWithPo(
        currencyScaleService.getCompanyScaledValue(
            budget, budgetLine.getRealizedWithPo().add(amount)));
    budgetLine.setAmountCommitted(
        currencyScaleService.getCompanyScaledValue(
            budget, budgetLine.getAmountCommitted().subtract(amount)));
    computeOtherFields(budget, budgetLine, amount);
  }

  protected void computeOtherFields(Budget budget, BudgetLine budgetLine, BigDecimal amount) {
    budgetLine.setAmountRealized(
        currencyScaleService.getCompanyScaledValue(
            budget, budgetLine.getAmountRealized().add(amount)));
    budgetLine.setToBeCommittedAmount(
        currencyScaleService.getCompanyScaledValue(
            budget, budgetLine.getToBeCommittedAmount().subtract(amount)));
    BigDecimal firmGap =
        currencyScaleService.getCompanyScaledValue(
            budget,
            budgetLine
                .getAmountExpected()
                .subtract(budgetLine.getRealizedWithPo().add(budgetLine.getRealizedWithNoPo())));
    budgetLine.setFirmGap(firmGap.signum() >= 0 ? BigDecimal.ZERO : firmGap.abs());
    budgetLine.setAvailableAmount(
        currencyScaleService.getCompanyScaledValue(
            budget,
            (budgetLine.getAvailableAmount().subtract(amount)).compareTo(BigDecimal.ZERO) > 0
                ? budgetLine.getAvailableAmount().subtract(amount)
                : BigDecimal.ZERO));
  }
}
