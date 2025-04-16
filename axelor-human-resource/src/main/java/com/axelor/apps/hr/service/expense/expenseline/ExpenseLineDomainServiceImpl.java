package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.StringHelper;

public class ExpenseLineDomainServiceImpl implements ExpenseLineDomainService {
  @Override
  public String getInvitedCollaborators(ExpenseLine expenseLine) {
    return "self.id IN ("
        + StringHelper.getIdListString(
            Beans.get(EmployeeFetchService.class)
                .getInvitedCollaboratorsDomain(expenseLine.getExpenseDate()))
        + ")";
  }
}
