package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class MoveComputeServiceImpl implements MoveComputeService {

  @Override
  public Map<String, Object> computeTotals(Move move) {

    Map<String, Object> values = new HashMap<>();
    if (move.getMoveLineList() == null) {
      return values;
    }
    values.put("$totalLines", move.getMoveLineList().size());

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

    Predicate<? super MoveLine> isDebitCreditFilter =
        ml -> ml.getCredit().compareTo(BigDecimal.ZERO) > 0;
    if (totalDebit.compareTo(totalCredit) > 0) {
      isDebitCreditFilter = ml -> ml.getDebit().compareTo(BigDecimal.ZERO) > 0;
    }

    BigDecimal totalCurrency =
        move.getMoveLineList().stream()
            .filter(isDebitCreditFilter)
            .map(MoveLine::getCurrencyAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalCurrency", totalCurrency);

    BigDecimal difference = totalDebit.subtract(totalCredit);
    values.put("$difference", difference);

    return values;
  }
}
