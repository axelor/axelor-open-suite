/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.common.ObjectUtils;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class MoveLineMassEntryServiceImpl implements MoveLineMassEntryService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void clearMoveLineMassEntryListAndAddNewLines(Move move, Integer temporaryMoveNumber) {
    move.getMoveLineMassEntryList()
        .removeIf(
            moveLineMassEntry ->
                Objects.equals(moveLineMassEntry.getTemporaryMoveNumber(), temporaryMoveNumber));
    move.getMoveLineMassEntryList()
        .addAll(
            this.convertMoveLinesIntoMoveLineMassEntry(
                move, move.getMoveLineList(), temporaryMoveNumber));
  }

  @Override
  public List<MoveLineMassEntry> convertMoveLinesIntoMoveLineMassEntry(
      Move move, List<MoveLine> moveLines, Integer temporaryMoveNumber) {
    List<MoveLineMassEntry> moveLineMassEntryList = new ArrayList<>();
    if (move != null && ObjectUtils.notEmpty(moveLines)) {
      for (MoveLine moveLine : moveLines) {
        moveLineMassEntryList.add(
            this.convertMoveLineIntoMoveLineMassEntry(move, moveLine, temporaryMoveNumber));
      }
    }
    return moveLineMassEntryList;
  }

  @Override
  public MoveLineMassEntry convertMoveLineIntoMoveLineMassEntry(
      Move move, MoveLine moveLine, Integer tempMoveNumber) {
    MoveLineMassEntry moveLineMassEntry = new MoveLineMassEntry();
    if (move != null && moveLine != null) {
      moveLineMassEntry.setMove(move);
      moveLineMassEntry.setMovePaymentMode(move.getPaymentMode());
      moveLineMassEntry.setMovePaymentCondition(move.getPaymentCondition());
      moveLineMassEntry.setDebit(moveLine.getDebit());
      moveLineMassEntry.setCredit(moveLine.getCredit());
      moveLineMassEntry.setTemporaryMoveNumber(tempMoveNumber);
      moveLineMassEntry.setMoveMassEntry(move);
      moveLineMassEntry.setPartner(moveLine.getPartner());
      moveLineMassEntry.setAccount(moveLine.getAccount());
      moveLineMassEntry.setDate(moveLine.getDate());
      moveLineMassEntry.setDueDate(moveLine.getDueDate());
      moveLineMassEntry.setCutOffEndDate(moveLine.getCutOffEndDate());
      moveLineMassEntry.setCutOffStartDate(moveLine.getCutOffStartDate());
      moveLineMassEntry.setCounter(moveLine.getCounter());
      moveLineMassEntry.setDescription(moveLine.getDescription());
      moveLineMassEntry.setInputAction(1);
      // TODO ajouter tous les autres champs
    }

    return moveLineMassEntry;
  }
}
