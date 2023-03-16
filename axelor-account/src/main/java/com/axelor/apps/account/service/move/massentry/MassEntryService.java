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
package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.exception.AxelorException;
import java.util.List;
import java.util.Map;

public interface MassEntryService {

  void fillMoveLineListWithMoveLineMassEntryList(Move move, Integer temporaryMoveNumber);

  MoveLineMassEntry getFirstMoveLineMassEntryInformations(
      List<MoveLineMassEntry> moveLineMassEntryList, MoveLineMassEntry moveLineMassEntry);

  void resetMoveLineMassEntry(MoveLineMassEntry moveLineMassEntry);

  void verifyFieldsChangeOnMoveLineMassEntry(Move move) throws AxelorException;

  void checkMassEntryMoveGeneration(Move move) throws AxelorException;

  Map<List<Long>, String> validateMassEntryMove(Move move);

  Long generateMassEntryMove(Move move) throws AxelorException;
}
