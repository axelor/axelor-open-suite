/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.weeklyplanning;

import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;

public interface WeeklyPlanningService {
  public WeeklyPlanning initPlanning(WeeklyPlanning planning);

  public WeeklyPlanning checkPlanning(WeeklyPlanning planning) throws AxelorException;

  public double workingDayValue(WeeklyPlanning planning, LocalDate date);

  public double workingDayValueWithSelect(
      WeeklyPlanning planning, LocalDate date, boolean morning, boolean afternoon);

  public DayPlanning findDayPlanning(WeeklyPlanning planning, LocalDate date);

  public DayPlanning findDayWithName(WeeklyPlanning planning, String name);
}
