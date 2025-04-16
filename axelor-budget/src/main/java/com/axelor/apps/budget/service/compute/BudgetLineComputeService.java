package com.axelor.apps.budget.service.compute;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.budget.db.Budget;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface BudgetLineComputeService {
  void updateBudgetLineAmounts(
      Move move,
      Budget budget,
      BigDecimal amount,
      LocalDate fromDate,
      LocalDate toDate,
      LocalDate defaultDate);

  void updateBudgetLineAmounts(
      InvoiceLine invoiceLine,
      Budget budget,
      BigDecimal amount,
      LocalDate fromDate,
      LocalDate toDate,
      LocalDate defaultDate);
}
