package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ExpenseCreateWizardServiceImpl implements ExpenseCreateWizardService {

  protected ExpenseToolService expenseToolService;
  protected ExpenseLineRepository expenseLineRepository;
  protected ExpenseCreateService expenseCreateService;

  @Inject
  public ExpenseCreateWizardServiceImpl(
      ExpenseToolService expenseToolService,
      ExpenseLineRepository expenseLineRepository,
      ExpenseCreateService expenseCreateService) {
    this.expenseToolService = expenseToolService;
    this.expenseLineRepository = expenseLineRepository;
    this.expenseCreateService = expenseCreateService;
  }

  @Override
  public ActionView.ActionViewBuilder getCreateExpenseWizard(Context context)
      throws AxelorException {

    List<Integer> idList = (List<Integer>) context.get("_ids");
    if (CollectionUtils.isEmpty(idList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_NO_LINE_SELECTED));
    }
    checkLines(idList);
    return createView(idList);
  }

  @Override
  public Expense createExpense(List<ExpenseLine> expenseLineList) throws AxelorException {
    Company company = getCompany();
    Optional<Currency> currency =
        expenseLineList.stream().map(ExpenseLine::getCurrency).findFirst();
    Optional<Employee> employee =
        expenseLineList.stream().map(ExpenseLine::getEmployee).findFirst();
    return expenseCreateService.createExpense(
        company,
        employee.orElse(null),
        currency.orElse(null),
        null,
        null,
        ExpenseRepository.COMPANY_CB_PAYMENT_NO,
        expenseLineList);
  }

  @Override
  public String getExpenseDomain(List<ExpenseLine> expenseLineList) {
    Long currencyId =
        expenseLineList.stream()
            .map(ExpenseLine::getCurrency)
            .map(Currency::getId)
            .findFirst()
            .orElse(0L);
    Long employeeId = getEmployeeId();
    return "self.statusSelect = "
        + ExpenseRepository.STATUS_DRAFT
        + " AND self.employee = "
        + employeeId
        + " AND self.currency.id = "
        + currencyId;
  }

  protected Long getEmployeeId() {
    Long employeeId = 0L;
    User user = AuthUtils.getUser();
    if (user != null) {
      Employee employee = user.getEmployee();
      if (employee != null) {
        employeeId = employee.getId();
      }
    }
    return employeeId;
  }

  protected Company getCompany() {
    User user = AuthUtils.getUser();
    Company company = null;
    if (user != null) {
      company = user.getActiveCompany();
    }
    return company;
  }

  protected void checkLines(List<Integer> idList) throws AxelorException {
    List<ExpenseLine> expenseLineList =
        expenseLineRepository.findByIds(
            idList.stream().map(Integer::longValue).collect(Collectors.toList()));
    checkCurrency(expenseLineList);
    checkEmployee(expenseLineList);
  }

  protected ActionView.ActionViewBuilder createView(List<Integer> idList) {
    ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Create expense"));
    actionViewBuilder.model(Wizard.class.getName());
    actionViewBuilder.add("form", "expense-line-merge-form");
    actionViewBuilder.param("popup", "reload");
    actionViewBuilder.param("show-toolbar", "false");
    actionViewBuilder.param("show-confirm", "false");
    actionViewBuilder.param("width", "large");
    actionViewBuilder.param("popup-save", "false");
    actionViewBuilder.context("_selectedLines", idList);
    return actionViewBuilder;
  }

  protected void checkCurrency(List<ExpenseLine> expenseLineList) throws AxelorException {
    if (expenseToolService.listHasSeveralCurrencies(expenseLineList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_SELECTED_CURRENCY_ERROR));
    }
  }

  protected void checkEmployee(List<ExpenseLine> expenseLineList) throws AxelorException {
    if (expenseToolService.listHasSeveralEmployees(expenseLineList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_SELECTED_EMPLOYEE_ERROR));
    }
  }
}
