package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.hr.db.Employee;

public class ExpenseLineDomainServiceImpl implements ExpenseLineDomainService {

  @Override
  public String getExpenseLineToMergeDomain(Company company, Currency currency, Employee employee) {
    Currency currencyCompany = company.getCurrency();
    if (currencyCompany.equals(currency)) {
      return "self.expense = null AND self.employee.id = "
          + employee.getId()
          + " AND  self.currency = "
          + currency.getId();
    } else {
      return "self.expense = null AND self.employee.id = "
          + employee.getId()
          + " AND  self.currency = "
          + currency.getId()
          + " AND self.isKilometricLine IS FALSE";
    }
  }
}
