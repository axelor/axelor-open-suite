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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Year;

public interface YearControlService {

  /**
   * Method that controls dates (fromDate and toDate). In case a changement occured while this year
   * is used in a Period that is used in move it will throw a Exception.
   *
   * @param asType
   * @throws AxelorException
   */
  void controlDates(Year year) throws AxelorException;

  /**
   * Method that checks if year is linked to {@link Move}
   *
   * @param year
   * @return true if year is linked, else false.
   */
  boolean isLinkedToMove(Year year);
}
