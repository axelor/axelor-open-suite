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
package com.axelor.apps.base.service.publicHoliday;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface PublicHolidayService {

  BigDecimal computePublicHolidayDays(
      LocalDate fromDate,
      LocalDate toDate,
      WeeklyPlanning weeklyPlanning,
      EventsPlanning publicHolidayPlanning);

  boolean checkPublicHolidayDay(LocalDate date, EventsPlanning publicHolidayEventsPlanning);

  void generateEventsPlanningLines(
      EventsPlanning eventsPlanning, LocalDate startDate, LocalDate endDate, String description);

  /**
   * This method will check if the current date is free of public holiday day and is a workind day
   * for company.<br>
   * If not the case, it will try next day until finding one and return it.
   *
   * @param date
   * @param company
   * @return holiday free local date.
   */
  LocalDate getFreeDay(LocalDate date, Company company);
}
