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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.common.ObjectUtils;
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

          // Check cut off dates
          if (moveLine.getCutOffStartDate() != null
              && moveLine.getCutOffEndDate() != null
              && (!moveLine.getCutOffStartDate().equals(moveLineIt.getCutOffStartDate())
                  || !moveLine.getCutOffEndDate().equals(moveLineIt.getCutOffEndDate()))) {
            return null;
          }

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
                if (checkAnalyticDistributionLine(
                    analyticDistributionLine, analyticDistributionLineIt)) {
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

    Map<List<Object>, MoveLine> map = new HashMap<>();
    MoveLine consolidateMoveLine = null;
    boolean haveHoldBack =
        moveLines.stream()
            .anyMatch(
                ml ->
                    ObjectUtils.notEmpty(ml.getMove().getPaymentCondition())
                        && ml.getMove().getPaymentCondition().getPaymentConditionLineList().stream()
                            .anyMatch(PaymentConditionLine::getIsHoldback));

    for (MoveLine moveLine : moveLines) {

      List<Object> keys = new ArrayList<>();

      keys.add(moveLine.getAccount());
      keys.add(moveLine.getTaxLine());
      keys.add(moveLine.getAnalyticDistributionTemplate());
      keys.add(moveLine.getCutOffStartDate());
      keys.add(moveLine.getCutOffEndDate());

      if (!haveHoldBack) {
        consolidateMoveLine = this.findConsolidateMoveLine(map, moveLine, keys);
      }

      if (consolidateMoveLine != null) {

        BigDecimal consolidateCurrencyAmount;

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
              if (checkAnalyticDistributionLine(
                  analyticDistributionLine, analyticDistributionLineIt)) {
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

    BigDecimal credit, debit;

    int moveLineId = 1;
    moveLines.clear();

    for (MoveLine moveLine : map.values()) {

      credit = moveLine.getCredit();
      debit = moveLine.getDebit();

      moveLine.setCurrencyAmount(moveLine.getCurrencyAmount().abs());

      if (debit.compareTo(BigDecimal.ZERO) > 0 && credit.compareTo(BigDecimal.ZERO) > 0) {

        if (debit.compareTo(credit) > 0) {
          moveLine.setDebit(debit.subtract(credit));
          moveLine.setCredit(BigDecimal.ZERO);
          moveLine.setCounter(moveLineId++);
          moveLines.add(moveLine);
        } else if (credit.compareTo(debit) > 0) {
          moveLine.setCredit(credit.subtract(debit));
          moveLine.setDebit(BigDecimal.ZERO);
          moveLine.setCounter(moveLineId++);
          moveLines.add(moveLine);
        }

      } else if (debit.compareTo(BigDecimal.ZERO) > 0 || credit.compareTo(BigDecimal.ZERO) > 0) {
        moveLine.setCounter(moveLineId++);
        moveLines.add(moveLine);
      }
    }

    return moveLines;
  }

  protected boolean checkAnalyticDistributionLine(
      AnalyticMoveLine analyticDistributionLine, AnalyticMoveLine analyticDistributionLineIt) {
    return analyticDistributionLine.getAnalyticAxis() != null
        && analyticDistributionLine
            .getAnalyticAxis()
            .equals(analyticDistributionLineIt.getAnalyticAxis())
        && analyticDistributionLine.getAnalyticAccount() != null
        && analyticDistributionLine
            .getAnalyticAccount()
            .equals(analyticDistributionLineIt.getAnalyticAccount())
        && analyticDistributionLine.getAccount() != null
        && analyticDistributionLine.getAccount().equals(analyticDistributionLineIt.getAccount())
        && analyticDistributionLine.getPercentage() != null
        && analyticDistributionLine
            .getPercentage()
            .equals(analyticDistributionLineIt.getPercentage())
        && ((analyticDistributionLine.getAnalyticJournal() == null
                && analyticDistributionLineIt.getAnalyticJournal() == null)
            || (analyticDistributionLine.getAnalyticJournal() != null
                && analyticDistributionLine
                    .getAnalyticJournal()
                    .equals(analyticDistributionLineIt.getAnalyticJournal())));
  }
}
