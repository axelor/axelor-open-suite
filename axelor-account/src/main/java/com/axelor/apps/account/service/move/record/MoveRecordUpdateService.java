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
package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;

public interface MoveRecordUpdateService {

  void updatePartner(Move move);

  String updateInvoiceTerms(Move move, boolean paymentConditionChange, boolean headerChange)
      throws AxelorException;

  void updateInvoiceTermDueDate(Move move, LocalDate dueDate);

  void updateInDayBookMode(Move move) throws AxelorException;

  void updateMoveLinesCurrencyRate(Move move) throws AxelorException;

  void updateDueDate(Move move, boolean paymentConditionChange, boolean dateChange)
      throws AxelorException;

  LocalDate getDateOfReversion(LocalDate moveDate, int dateOfReversionSelect);

  void resetDueDate(Move move);
}
