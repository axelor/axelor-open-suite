package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class MoveComputeServiceImpl implements MoveComputeService {

  @Override
  public Map<String, Object> computeTotals(Move move) {

    Map<String, Object> values = new HashMap<>();
    if (move.getMoveLineList() == null) {
      return values;
    }
    values.put("$totalLines", move.getMoveLineList().size());

    BigDecimal totalCurrency =
        move.getMoveLineList().stream()
            .map(MoveLine::getCurrencyAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
    values.put("$totalCurrency", totalCurrency);

    BigDecimal totalDebit =
        move.getMoveLineList().stream()
            .map(MoveLine::getDebit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalDebit", totalDebit);

    BigDecimal totalCredit =
        move.getMoveLineList().stream()
            .map(MoveLine::getCredit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalCredit", totalCredit);

    BigDecimal difference = totalDebit.subtract(totalCredit);
    values.put("$difference", difference);

    return values;
  }
}
