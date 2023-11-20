package com.axelor.apps.hr.web.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.expense.ExpenseCreateService;
import com.axelor.apps.hr.service.expense.ExpenseLineDomainService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ExpenseCreateController {

  public void createExpense(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    List<ExpenseLine> expenseLineToMerge =
        MapHelper.getCollection(context, ExpenseLine.class, "expenseLinesToMerge");
    if (CollectionUtils.isEmpty(expenseLineToMerge)) {
      response.setError(I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_NO_LINE_SELECTED));
    } else {
      Company company = MapHelper.get(context, Company.class, "company");
      Employee employee = MapHelper.get(context, Employee.class, "employee");
      Currency currency = MapHelper.get(context, Currency.class, "currency");
      Period period = MapHelper.get(context, Period.class, "period");
      BankDetails bankDetails = MapHelper.get(context, BankDetails.class, "bankDetails");
      Integer companyCbSelect = MapHelper.get(context, Integer.class, "companyCbSelect");

      Expense expense =
          Beans.get(ExpenseCreateService.class)
              .createExpense(
                  company,
                  employee,
                  currency,
                  bankDetails,
                  period,
                  companyCbSelect,
                  expenseLineToMerge);

      if (expense != null) {
        response.setView(
            ActionView.define(I18n.get("Expense"))
                .model(Expense.class.getName())
                .add("grid", "expense-grid")
                .add("form", "expense-form")
                .param("search-filters", "expense-filters")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(expense.getId()))
                .map());
        response.setCanClose(true);
      }
    }
  }

  public void fillBankDetailsFromEmployee(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Employee employee = MapHelper.get(context, Employee.class, "employee");
    response.setValue("bankDetails", Beans.get(EmployeeService.class).getBankDetails(employee));
  }

  public void getBankDetailsDomain(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Company company = MapHelper.get(context, Company.class, "company");
    String domain = Beans.get(BankDetailsService.class).getActiveCompanyBankDetails(company);

    if (domain.equals("")) {
      response.setAttr("bankDetails", "domain", "self.id IN (0)");
    } else {
      response.setAttr("bankDetails", "domain", domain);
    }
  }

  public void getExpenseLineDomain(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Company company = MapHelper.get(context, Company.class, "company");
    Employee employee = MapHelper.get(context, Employee.class, "employee");
    Currency currency = MapHelper.get(context, Currency.class, "currency");
    response.setAttr(
        "$expenseLinesToMerge",
        "domain",
        Beans.get(ExpenseLineDomainService.class)
            .getExpenseLineToMergeDomain(company, currency, employee));
  }
}
