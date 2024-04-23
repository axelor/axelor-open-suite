package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import java.math.BigDecimal;

public interface ReconcileToolBudgetService {

  BigDecimal computeReconcileRatio(Invoice invoice, Move move, BigDecimal amount);
}
