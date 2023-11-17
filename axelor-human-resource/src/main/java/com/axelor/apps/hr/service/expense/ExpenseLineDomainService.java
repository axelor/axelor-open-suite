package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.hr.db.Employee;

public interface ExpenseLineDomainService {
  String getExpenseLineToMergeDomain(Company company, Currency currency, Employee employee);
}
