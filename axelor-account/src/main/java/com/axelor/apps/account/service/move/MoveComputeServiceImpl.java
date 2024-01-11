/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
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
