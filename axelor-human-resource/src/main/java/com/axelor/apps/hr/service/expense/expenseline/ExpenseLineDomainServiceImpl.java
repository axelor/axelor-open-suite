package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;

import java.util.List;

public class ExpenseLineDomainServiceImpl implements ExpenseLineDomainService {
  protected EmployeeFetchService employeeFetchService;

  @Inject
  public ExpenseLineDomainServiceImpl(EmployeeFetchService employeeFetchService) {
    this.employeeFetchService = employeeFetchService;
  }

  @Override
  public String getInvitedCollaborators(ExpenseLine expenseLine, Expense expense) {
  List<Employee> employeeList=  employeeFetchService.getInvitedCollaborators(expenseLine.getExpenseDate());
     if(expense!=null)
     {employeeList.remove(expense.getEmployee());}
    return "self.id IN ("
        + StringHelper.getIdListString(employeeList)
        + ")";
  }
}
