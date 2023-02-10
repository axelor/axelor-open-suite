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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Company;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface AccountingCutOffService {

  Query<Move> getMoves(
      Company company, Journal researchJournal, LocalDate moveDate, int accountingCutOffTypeSelect);

  @Transactional(rollbackOn = {Exception.class})
  List<Move> generateCutOffMovesFromMove(
      Move move,
      Journal journal,
      LocalDate moveDate,
      LocalDate reverseMoveDate,
      String moveDescription,
      String reverseMoveDescription,
      int accountingCutOffTypeSelect,
      int cutOffMoveStatusSelect,
      boolean automaticReverse,
      boolean automaticReconcile,
      String prefixOrigin)
      throws AxelorException;

  Move generateCutOffMoveFromMove(
      Move move,
      Journal journal,
      LocalDate moveDate,
      LocalDate originMoveDate,
      String moveDescription,
      int accountingCutOffTypeSelect,
      int cutOffMoveStatusSelect,
      boolean isReverse,
      String prefixOrigin)
      throws AxelorException;

  Query<MoveLine> getMoveLines(
      Company company, Journal researchJournal, LocalDate moveDate, int accountingCutOffTypeSelect);
}
