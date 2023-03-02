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
import java.util.List;

/** Interface of service to create {@link MoveLineMassEntry} */
public interface MoveLineMassEntryService {

  void clearMoveLineMassEntryListAndAddNewLines(Move move, Integer temporaryMoveNumber);

  void sortMoveLinesMassEntryByTemporaryNumber(Move move);

  List<MoveLineMassEntry> convertMoveLinesIntoMoveLineMassEntry(
      Move move, List<MoveLine> moveLines, Integer temporaryMoveNumber);

  MoveLineMassEntry convertMoveLineIntoMoveLineMassEntry(
      Move move, MoveLine moveLine, Integer temporaryMoveNumber);
}
