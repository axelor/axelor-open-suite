package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLimit;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLimitRepository;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.i18n.L10n;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ExpenseLimitServiceImpl implements ExpenseLimitService {
  protected ExpenseLimitRepository expenseLimitRepository;
  protected ExpenseLineRepository expenseLineRepository;

  @Inject
  public ExpenseLimitServiceImpl(
      ExpenseLimitRepository expenseLimitRepository, ExpenseLineRepository expenseLineRepository) {
    this.expenseLimitRepository = expenseLimitRepository;
    this.expenseLineRepository = expenseLineRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void checkExpenseLimit(Expense expense) throws AxelorException {
    List<ExpenseLimit> expenseLimitList =
        expenseLimitRepository
            .all()
            .filter("self.employee = :employee")
            .bind("employee", expense.getEmployee())
            .fetch();
    if (CollectionUtils.isEmpty(expenseLimitList)) {
      return;
    }

    List<ExpenseLine> expenseLineList = getExpenseLines(expense);
    checkExpenseLimits(expenseLimitList, expenseLineList);
  }

  protected void checkExpenseLimits(
      List<ExpenseLimit> expenseLimitList, List<ExpenseLine> expenseLineList)
      throws AxelorException {
    for (ExpenseLimit expenseLimit : expenseLimitList) {
      LocalDate fromDate = expenseLimit.getFromDate();
      LocalDate toDate = expenseLimit.getToDate();
      L10n dateFormat = L10n.getInstance();

      BigDecimal totalAmount =
          expenseLineList.stream()
              .filter(line -> LocalDateHelper.isBetween(fromDate, toDate, line.getExpenseDate()))
              .map(ExpenseLine::getTotalAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      if (totalAmount.compareTo(expenseLimit.getMaxAmount()) > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(HumanResourceExceptionMessage.EXPENSE_LIMIT_EXCEEDED),
                dateFormat.format(fromDate),
                dateFormat.format(toDate)));
      }
    }
  }

  protected List<ExpenseLine> getExpenseLines(Expense expense) {
    List<ExpenseLine> expenseLineList = new ArrayList<>();
    List<ExpenseLine> generalExpenseLineList = expense.getGeneralExpenseLineList();
    List<ExpenseLine> kilometricExpenseLineList = expense.getKilometricExpenseLineList();
    if (CollectionUtils.isNotEmpty(generalExpenseLineList)) {
      expenseLineList.addAll(generalExpenseLineList);
    }
    if (CollectionUtils.isNotEmpty(kilometricExpenseLineList)) {
      expenseLineList.addAll(kilometricExpenseLineList);
    }
    addDatabaseLines(expense, expenseLineList);
    return expenseLineList;
  }

  protected void addDatabaseLines(Expense expense, List<ExpenseLine> expenseLineList) {
    List<ExpenseLine> dbExpenseLineList =
        expenseLineRepository
            .all()
            .filter(
                "self.employee = :employee AND self.expense.statusSelect = :expenseValidatedStatus AND self.expense != :expense")
            .bind("employee", expense.getEmployee())
            .bind("expenseValidatedStatus", ExpenseRepository.STATUS_VALIDATED)
            .bind("expense", expense)
            .fetch();
    if (CollectionUtils.isNotEmpty(dbExpenseLineList)) {
      expenseLineList.addAll(dbExpenseLineList);
    }
  }
}
