/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.google.inject.Inject;
import java.time.LocalDate;
import org.apache.commons.collections.CollectionUtils;

public class MoveCutOffServiceImpl implements MoveCutOffService {
  protected MoveLineService moveLineService;

  @Inject
  public MoveCutOffServiceImpl(MoveLineService moveLineService) {
    this.moveLineService = moveLineService;
  }

  @Override
  public boolean checkManageCutOffDates(Move move) {
    return CollectionUtils.isNotEmpty(move.getMoveLineList())
        && move.getMoveLineList().stream()
            .anyMatch(moveLine -> moveLineService.checkManageCutOffDates(moveLine));
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

  @Override
  public void autoApplyCutOffDates(Move move) {
    move.getMoveLineList().stream()
        .filter(
            moveLine ->
                moveLine.getAccount().getManageCutOffPeriod()
                    && moveLine.getAccount().getHasAutomaticApplicationAccountingDate()
                    && moveLine.getCutOffStartDate() == null
                    && moveLine.getCutOffEndDate() == null)
        .forEach(
            moveLine -> {
              LocalDate cutOffDate = move.getDate();
              moveLineService.applyCutOffDates(moveLine, move, cutOffDate, cutOffDate);
              moveLine.setIsCutOffGenerated(true);
            });
  }

  @Override
  public void applyCutOffDatesInEmptyLines(
      Move move, LocalDate cutOffStartDate, LocalDate cutOffEndDate) {
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      move.getMoveLineList().stream()
          .filter(ml -> (ml.getCutOffStartDate() == null || ml.getCutOffEndDate() == null))
          .forEach(
              moveLine ->
                  moveLineService.applyCutOffDates(moveLine, move, cutOffStartDate, cutOffEndDate));
    }
  }
}
