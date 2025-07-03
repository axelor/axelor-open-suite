/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import java.util.List;

public interface MassEntryToolService {

  void clearMoveLineMassEntryListAndAddNewLines(
      Move parentMove, Move childMove, Integer temporaryMoveNumber);

  void sortMoveLinesMassEntryByTemporaryNumber(Move move);

  List<MoveLineMassEntry> convertMoveLinesIntoMoveLineMassEntry(
      Move move, List<MoveLine> moveLineList, Integer temporaryMoveNumber);

  MoveLineMassEntry convertMoveLineIntoMoveLineMassEntry(
      Move move, MoveLine moveLine, Integer temporaryMoveNumber);

  List<MoveLineMassEntry> getEditedMoveLineMassEntry(List<MoveLineMassEntry> moveLineList);

  void fillMassEntryLinesFields(Move parentMove, Move move, Integer newStatusSelect);

  boolean verifyJournalAuthorizeNewMove(List<MoveLineMassEntry> moveLineList, Journal journal);

  Integer getMaxTemporaryMoveNumber(List<MoveLineMassEntry> moveLineList);
}
