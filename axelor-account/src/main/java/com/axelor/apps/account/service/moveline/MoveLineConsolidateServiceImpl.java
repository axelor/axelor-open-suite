/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineConsolidateServiceImpl implements MoveLineConsolidateService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public MoveLine findConsolidateMoveLine(
      Map<List<Object>, MoveLine> map, MoveLine moveLine, List<Object> keys) {
    if (map != null && !map.isEmpty()) {

      Map<List<Object>, MoveLine> copyMap = new HashMap<List<Object>, MoveLine>(map);
      while (!copyMap.isEmpty()) {

        if (map.containsKey(keys)) {

          MoveLine moveLineIt = map.get(keys);
          int count = 0;
          if (moveLineIt.getAnalyticMoveLineList() == null
              && moveLine.getAnalyticMoveLineList() == null) {
            return moveLineIt;
          } else if (moveLineIt.getAnalyticMoveLineList() == null
              || moveLine.getAnalyticMoveLineList() == null) {
            break;
          }
          List<AnalyticMoveLine> list1 = moveLineIt.getAnalyticMoveLineList();
          List<AnalyticMoveLine> list2 = moveLine.getAnalyticMoveLineList();
          List<AnalyticMoveLine> copyList = new ArrayList<AnalyticMoveLine>(list1);
          if (list1.size() == list2.size()) {
            for (AnalyticMoveLine analyticDistributionLine : list2) {
              for (AnalyticMoveLine analyticDistributionLineIt : copyList) {
                if (analyticDistributionLine
                        .getAnalyticAxis()
                        .equals(analyticDistributionLineIt.getAnalyticAxis())
                    && analyticDistributionLine
                        .getAnalyticAccount()
                        .equals(analyticDistributionLineIt.getAnalyticAccount())
                    && analyticDistributionLine
                        .getAccount()
                        .equals(analyticDistributionLineIt.getAccount())
                    && analyticDistributionLine
                        .getPercentage()
                        .equals(analyticDistributionLineIt.getPercentage())
                    && ((analyticDistributionLine.getAnalyticJournal() == null
                            && analyticDistributionLineIt.getAnalyticJournal() == null)
                        || analyticDistributionLine
                            .getAnalyticJournal()
                            .equals(analyticDistributionLineIt.getAnalyticJournal()))) {
                  copyList.remove(analyticDistributionLineIt);
                  count++;
                  break;
                }
              }
            }
            if (count == list1.size()) {
              return moveLineIt;
            }
          }
        } else {
          return null;
        }
      }
    }

    return null;
  }

  /**
   * Consolider des lignes d'écritures par compte comptable.
   *
   * @param moveLines
   */
  @Override
  public List<MoveLine> consolidateMoveLines(List<MoveLine> moveLines) {

    Map<List<Object>, MoveLine> map = new HashMap<List<Object>, MoveLine>();
    MoveLine consolidateMoveLine = null;

    for (MoveLine moveLine : moveLines) {

      List<Object> keys = new ArrayList<Object>();

      keys.add(moveLine.getAccount());
      keys.add(moveLine.getTaxLine());
      keys.add(moveLine.getAnalyticDistributionTemplate());

      consolidateMoveLine = this.findConsolidateMoveLine(map, moveLine, keys);
      if (consolidateMoveLine != null) {

        BigDecimal consolidateCurrencyAmount = BigDecimal.ZERO;

        log.debug(
            "MoveLine :: Debit : {}, Credit : {}, Currency amount : {}",
            moveLine.getDebit(),
            moveLine.getCredit(),
            moveLine.getCurrencyAmount());
        log.debug(
            "Consolidate moveLine :: Debit : {}, Credit : {}, Currency amount : {}",
            consolidateMoveLine.getDebit(),
            consolidateMoveLine.getCredit(),
            consolidateMoveLine.getCurrencyAmount());

        if (moveLine.getDebit().subtract(moveLine.getCredit()).compareTo(BigDecimal.ZERO)
            != consolidateMoveLine
                .getDebit()
                .subtract(consolidateMoveLine.getCredit())
                .compareTo(BigDecimal.ZERO)) {
          consolidateCurrencyAmount =
              consolidateMoveLine.getCurrencyAmount().subtract(moveLine.getCurrencyAmount());
        } else {
          consolidateCurrencyAmount =
              consolidateMoveLine.getCurrencyAmount().add(moveLine.getCurrencyAmount());
        }
        consolidateMoveLine.setCurrencyAmount(consolidateCurrencyAmount.abs());
        consolidateMoveLine.setCredit(consolidateMoveLine.getCredit().add(moveLine.getCredit()));
        consolidateMoveLine.setDebit(consolidateMoveLine.getDebit().add(moveLine.getDebit()));

        if (consolidateMoveLine.getAnalyticMoveLineList() != null
            && !consolidateMoveLine.getAnalyticMoveLineList().isEmpty()) {
          for (AnalyticMoveLine analyticDistributionLine :
              consolidateMoveLine.getAnalyticMoveLineList()) {
            for (AnalyticMoveLine analyticDistributionLineIt : moveLine.getAnalyticMoveLineList()) {
              if (analyticDistributionLine
                      .getAnalyticAxis()
                      .equals(analyticDistributionLineIt.getAnalyticAxis())
                  && analyticDistributionLine
                      .getAnalyticAccount()
                      .equals(analyticDistributionLineIt.getAnalyticAccount())
                  && analyticDistributionLine
                      .getAccount()
                      .equals(analyticDistributionLineIt.getAccount())
                  && analyticDistributionLine
                      .getPercentage()
                      .equals(analyticDistributionLineIt.getPercentage())
                  && ((analyticDistributionLine.getAnalyticJournal() == null
                          && analyticDistributionLineIt.getAnalyticJournal() == null)
                      || analyticDistributionLine
                          .getAnalyticJournal()
                          .equals(analyticDistributionLineIt.getAnalyticJournal()))) {
                analyticDistributionLine.setAmount(
                    analyticDistributionLine
                        .getAmount()
                        .add(analyticDistributionLineIt.getAmount()));
                break;
              }
            }
          }
        }
      } else {
        map.put(keys, moveLine);
      }
    }

    BigDecimal credit = null;
    BigDecimal debit = null;

    int moveLineId = 1;
    moveLines.clear();

    for (MoveLine moveLine : map.values()) {

      credit = moveLine.getCredit();
      debit = moveLine.getDebit();

      moveLine.setCurrencyAmount(moveLine.getCurrencyAmount().abs());

      if (debit.compareTo(BigDecimal.ZERO) == 1 && credit.compareTo(BigDecimal.ZERO) == 1) {

        if (debit.compareTo(credit) == 1) {
          moveLine.setDebit(debit.subtract(credit));
          moveLine.setCredit(BigDecimal.ZERO);
          moveLine.setCounter(moveLineId++);
          moveLines.add(moveLine);
        } else if (credit.compareTo(debit) == 1) {
          moveLine.setCredit(credit.subtract(debit));
          moveLine.setDebit(BigDecimal.ZERO);
          moveLine.setCounter(moveLineId++);
          moveLines.add(moveLine);
        }

      } else if (debit.compareTo(BigDecimal.ZERO) == 1 || credit.compareTo(BigDecimal.ZERO) == 1) {
        moveLine.setCounter(moveLineId++);
        moveLines.add(moveLine);
      }
    }

    return moveLines;
  }
}
