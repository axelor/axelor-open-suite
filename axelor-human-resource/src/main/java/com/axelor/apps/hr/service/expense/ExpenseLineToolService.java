package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.meta.db.MetaFile;
import java.math.BigDecimal;

public interface ExpenseLineToolService {
  void computeAmount(Employee employee, ExpenseLine expenseLine) throws AxelorException;

  void computeDistance(BigDecimal distance, ExpenseLine expenseLine) throws AxelorException;

  void setGeneralExpenseLineInfo(
      Product expenseProduct,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      MetaFile justificationMetaFile,
      ExpenseLine expenseLine)
      throws AxelorException;

  boolean isKilometricExpenseLine(ExpenseLine expenseLine);
}
