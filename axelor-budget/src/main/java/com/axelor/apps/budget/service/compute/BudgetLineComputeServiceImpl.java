package com.axelor.apps.budget.service.compute;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
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
  public void updateBudgetLineAmounts(
      InvoiceLine invoiceLine,
      Budget budget,
      BigDecimal amount,
      LocalDate fromDate,
      LocalDate toDate,
      LocalDate defaultDate) {
    ComputeMethod computeMethod = this::updateBudgetLineAmountsWithNoPo;
    Invoice invoice = Optional.ofNullable(invoiceLine).map(InvoiceLine::getInvoice).orElse(null);
    if (invoice != null
        && (invoice.getPurchaseOrder() != null
            || invoice.getSaleOrder() != null
            || invoiceLine.getPurchaseOrderLine() != null
            || invoiceLine.getSaleOrderLine() != null)) {
      computeMethod = this::updateBudgetLineAmountsWithPo;
    }

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
      long totalDuration = LocalDateHelper.daysBetween(fromDate, toDate, false);
      LocalDate date = fromDate;
      BigDecimal prorataAmount;
      BigDecimal missingAmount = amount;

      while (!date.isAfter(toDate)) {
        BudgetLine budgetLine = budgetLineRepository.findCurrentByDate(budget, date);
        if (budgetLine != null && totalDuration > 0) {
          if (toDate.isBefore(budgetLine.getToDate())) {
            prorataAmount = missingAmount;
          } else {
            long duration = LocalDateHelper.daysBetween(date, budgetLine.getToDate(), false);
            prorataAmount =
                amount.multiply(
                    BigDecimal.valueOf(duration)
                        .divide(
                            BigDecimal.valueOf(totalDuration),
                            AppBaseService.COMPUTATION_SCALING,
                            RoundingMode.HALF_UP));
            missingAmount = missingAmount.subtract(prorataAmount);
            BigDecimal gap = BigDecimal.valueOf(0.01);
            if (missingAmount.compareTo(gap) == 0) {
              prorataAmount = prorataAmount.add(gap);
            }
          }
          computeMethod.computeBudgetLineAmounts(budgetLine, prorataAmount);
          date = budgetLine.getToDate().plusDays(1);
        } else {
          break;
        }
      }
    } else {
      BudgetLine budgetLine = budgetLineRepository.findCurrentByDate(budget, defaultDate);
      computeMethod.computeBudgetLineAmounts(budgetLine, amount);
    }
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
