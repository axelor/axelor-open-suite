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
package com.axelor.apps.base.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.meta.CallMethod;
import java.time.LocalDate;

public interface PeriodUtilsService {

  /**
   * Recupère la bonne période pour la date passée en paramètre
   *
   * @param date
   * @param company
   * @return
   * @throws AxelorException
   */
  Period getActivePeriod(LocalDate date, Company company, int typeSelect) throws AxelorException;

  Period getPeriod(LocalDate date, Company company, int typeSelect);

  /**
   * @param period
   * @throws AxelorException if the period is permanently or temporally closed
   */
  @CallMethod
  boolean isClosedPeriod(Period period) throws AxelorException;
}
