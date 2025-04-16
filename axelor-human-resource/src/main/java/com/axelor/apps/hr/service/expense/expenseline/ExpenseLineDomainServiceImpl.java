package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;

public class ExpenseLineDomainServiceImpl implements ExpenseLineDomainService {
  protected EmployeeFetchService employeeFetchService;

  @Inject
  public ExpenseLineDomainServiceImpl(EmployeeFetchService employeeFetchService) {
    this.employeeFetchService = employeeFetchService;
  }

  @Override
  public String getInvitedCollaborators(ExpenseLine expenseLine) {
    return "self.id IN ("
        + StringHelper.getIdListString(
            employeeFetchService.getInvitedCollaboratorsDomain(expenseLine.getExpenseDate()))
        + ")";
  }
}
