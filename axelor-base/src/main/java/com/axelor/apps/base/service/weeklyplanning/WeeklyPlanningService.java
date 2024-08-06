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
package com.axelor.apps.base.service.weeklyplanning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public interface WeeklyPlanningService {

  /**
   * Gets the first day of the week, according to the weekly planning of the active company of the
   * user. If no day can be found, default value is MONDAY.
   *
   * @return
   */
  DayOfWeek getFirstDayOfWeek();

  public WeeklyPlanning initPlanning(WeeklyPlanning planning);

  public WeeklyPlanning checkPlanning(WeeklyPlanning planning) throws AxelorException;

  /**
   * Computes the working value of the given day for the input date, according to the weekly
   * planning.
   *
   * @param planning
   * @param date
   * @return
   */
  public double getWorkingDayValueInDays(WeeklyPlanning planning, LocalDate date);

  /**
   * Computes the working value of the given day, according to the weekly planning and whether
   * morning and/or afternoon should be taken into account.
   *
   * @param planning
   * @param date
   * @param morning
   * @param afternoon
   * @return
   */
  public double getWorkingDayValueInDaysWithSelect(
      WeeklyPlanning planning, LocalDate date, boolean morning, boolean afternoon);

  /**
   * Computes the working value of the given day, within the given times.
   *
   * <p>Useful to compute leave duration. For the considered day, the leave begins at <em>from</em>
   * and ends at <em>to</em>. If <em>from</em> (respectively <em>to</em>) is null, it is assumed
   * that the leave begins (resp. ends) at the same time as this working day.
   *
   * @param weeklyPlanning
   * @param date
   * @param from
   * @param to
   * @return
   */
  public BigDecimal getWorkingDayValueInHours(
      WeeklyPlanning weeklyPlanning, LocalDate date, LocalTime from, LocalTime to);

  public DayPlanning findDayPlanning(WeeklyPlanning planning, LocalDate date);

  public DayPlanning findDayWithName(WeeklyPlanning planning, String name);
}
