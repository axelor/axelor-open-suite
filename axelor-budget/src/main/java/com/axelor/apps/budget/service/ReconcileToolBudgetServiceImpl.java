package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.commons.collections.CollectionUtils;

public class ReconcileToolBudgetServiceImpl implements ReconcileToolBudgetService {

  private final int CALCULATION_SCALE = 10;

  @Override
  public BigDecimal computeReconcileRatio(Invoice invoice, Move move, BigDecimal amount) {
    BigDecimal ratio = BigDecimal.ZERO;
    BigDecimal totalAmount = BigDecimal.ZERO;
    if (invoice != null) {
      totalAmount = invoice.getCompanyInTaxTotal();
    } else if (!CollectionUtils.isEmpty(move.getMoveLineList())) {
      totalAmount =
          move.getMoveLineList().stream()
              .map(MoveLine::getDebit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
    }

    if (totalAmount.signum() > 0) {
      ratio = amount.divide(totalAmount, CALCULATION_SCALE, RoundingMode.HALF_UP);
    }

    return ratio;
  }
}
