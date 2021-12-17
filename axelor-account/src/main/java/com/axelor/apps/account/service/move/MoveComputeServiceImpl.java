package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class MoveComputeServiceImpl implements MoveComputeService {
  protected MoveLineService moveLineService;

  @Inject
  public MoveComputeServiceImpl(MoveLineService moveLineService) {
    this.moveLineService = moveLineService;
  }

  @Override
  public Map<String, Object> computeTotals(Move move) {

    Map<String, Object> values = new HashMap<>();
    if (move.getMoveLineList() == null || move.getMoveLineList().isEmpty()) {
      return values;
    }
    values.put("$totalLines", move.getMoveLineList().size());

    BigDecimal totalCurrency =
        move.getMoveLineList().stream()
            .map(MoveLine::getCurrencyAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalCurrency", totalCurrency.divide(BigDecimal.ONE.add(BigDecimal.ONE)));

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

  @Override
  public boolean checkManageCutOffDates(Move move) {
    return CollectionUtils.isNotEmpty(move.getMoveLineList())
        && move.getMoveLineList().stream()
            .allMatch(invoiceLine -> moveLineService.checkManageCutOffDates(invoiceLine));
  }

  @Override
  public void applyCutOffDates(Move move, LocalDate cutOffStartDate, LocalDate cutOffEndDate) {
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      move.getMoveLineList()
          .forEach(
              moveLine ->
                  moveLineService.applyCutOffDates(moveLine, move, cutOffStartDate, cutOffEndDate));
    }
  }
}
