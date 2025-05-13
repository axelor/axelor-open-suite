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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayHrService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;

public class WorkingDayServiceImpl implements WorkingDayService {
  protected final WeeklyPlanningService weeklyPlanningService;
  protected final PublicHolidayHrService publicHolidayHrService;

  @Inject
  public WorkingDayServiceImpl(
      WeeklyPlanningService weeklyPlanningService, PublicHolidayHrService publicHolidayHrService) {
    this.weeklyPlanningService = weeklyPlanningService;
    this.publicHolidayHrService = publicHolidayHrService;
  }

  @Override
  public boolean isWorkingDay(Employee employee, LocalDate date) throws AxelorException {
    WeeklyPlanning planning = employee.getWeeklyPlanning();
    if (planning == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_PLANNING),
          employee.getName());
    }
    return isWorkingDay(weeklyPlanningService.findDayPlanning(planning, date))
        && !publicHolidayHrService.checkPublicHolidayDay(date, employee);
  }

  protected boolean isWorkingDay(DayPlanning dayPlanning) {
    if (dayPlanning == null) {
      return false;
    }
    return dayPlanning.getMorningFrom() != null
        || dayPlanning.getMorningTo() != null
        || dayPlanning.getAfternoonFrom() != null
        || dayPlanning.getAfternoonTo() != null;
  }
}
