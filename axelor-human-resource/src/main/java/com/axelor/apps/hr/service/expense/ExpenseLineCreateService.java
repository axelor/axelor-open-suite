package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.project.db.Project;
import com.axelor.meta.db.MetaFile;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseLineCreateService {

  ExpenseLine createGeneralExpenseLine(
      Project project,
      Product expenseProduct,
      LocalDate expenseDate,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      MetaFile justificationMetaFile,
      String comments,
      Employee employee,
      Currency currency,
      Boolean toInvoice)
      throws AxelorException;

  ExpenseLine createKilometricExpenseLine(
      Project project,
      LocalDate expenseDate,
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      BigDecimal distance,
      String fromCity,
      String toCity,
      String comments,
      Employee employee,
      Company company,
      Currency currency,
      Boolean toInvoice)
      throws AxelorException;
}
