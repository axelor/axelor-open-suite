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
package com.axelor.apps.account.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;

public interface PeriodControlService {

  /**
   * Method that control dates (fromDate and toDate) of period. It will throw an exception if a
   * changement occured while the period is linked to a move.
   *
   * @param period
   */
  void controlDates(Period period) throws AxelorException;

  /**
   * Checks if a Move is linked to period
   *
   * @param entity
   * @return
   */
  boolean isLinkedToMove(Period period);

  /**
   * Method that checks is statusSelect and year.statusSelect are greater or equal to 1 (OPENED) .
   *
   * @param period
   * @return true if status select is greater or equal to 1, else false;
   */
  boolean isStatusValid(Period period);
}
