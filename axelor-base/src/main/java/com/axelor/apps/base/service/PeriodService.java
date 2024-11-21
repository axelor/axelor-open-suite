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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.meta.CallMethod;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public interface PeriodService {

  /**
   * Recupère la bonne période pour la date passée en paramètre
   *
   * @param date
   * @param company
   * @return
   * @throws AxelorException
   */
  public Period getActivePeriod(LocalDate date, Company company, int typeSelect)
      throws AxelorException;

  public Period getPeriod(LocalDate date, Company company, int typeSelect);

  public Period getNextPeriod(Period period) throws AxelorException;

  public void testOpenPeriod(Period period) throws AxelorException;

  public void close(Period period);

  public void closeTemporarily(Period period) throws AxelorException;

  @Transactional
  public void adjust(Period period);

  /**
   * Check if the period corresponding to the date and the company is closed
   *
   * @param company
   * @param date
   * @throws AxelorException
   */
  public void checkPeriod(Company company, LocalDate date) throws AxelorException;

  /**
   * Check if the periods corresponding to the dates and the company are closed.
   *
   * @param company
   * @param fromDate
   * @param toDate
   */
  public void checkPeriod(Company company, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  /**
   * @param period
   * @throws AxelorException if the period is closed
   */
  public void checkPeriod(Period period) throws AxelorException;

  /**
   * @param period
   * @throws AxelorException if the period is permanently or temporally closed
   */
  @CallMethod
  boolean isClosedPeriod(Period period) throws AxelorException;

  public void validateTempClosure(Period period) throws AxelorException;

  public void validateClosure(Period period) throws AxelorException;

  /**
   * Method thats open the period
   *
   * @param period
   */
  void openPeriod(Period period);

  void closureInProgress(Period period);

  void closePeriod(Period period);

  void resetStatusSelect(Period period);
}
