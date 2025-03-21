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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;

public interface MoveLineRecordService {
  void setCurrencyFields(MoveLine moveLine, Move move) throws AxelorException;

  void setCutOffDates(MoveLine moveLine, LocalDate cutOffStartDate, LocalDate cutOffEndDate);

  void setIsCutOffGeneratedFalse(MoveLine moveLine);

  void refreshAccountInformation(MoveLine moveLine, Move move) throws AxelorException;

  void setParentFromMove(MoveLine moveLine, Move move);

  void setOriginDate(MoveLine moveLine);

  void setDebitCredit(MoveLine moveLine);

  void resetCredit(MoveLine moveLine);

  void resetDebit(MoveLine moveLine);

  void resetPartnerFields(MoveLine moveLine);

  void setCounter(MoveLine moveLine, Move move);

  void setMoveLineDates(Move move) throws AxelorException;

  void setMoveLineOriginDates(Move move) throws AxelorException;

  void computeDate(MoveLine moveLine, Move move) throws AxelorException;
}
