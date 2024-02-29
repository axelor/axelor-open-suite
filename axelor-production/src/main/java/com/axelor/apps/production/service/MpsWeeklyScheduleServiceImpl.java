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
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.MpsWeeklySchedule;
import java.math.BigDecimal;
import java.time.DayOfWeek;

public class MpsWeeklyScheduleServiceImpl implements MpsWeeklyScheduleService {

  @Override
  public void countTotalHours(MpsWeeklySchedule mpsWeeklySchedule) {

    mpsWeeklySchedule.setTotalHours(
        BigDecimal.ZERO
            .add(mpsWeeklySchedule.getHoursMonday())
            .add(mpsWeeklySchedule.getHoursTuesday())
            .add(mpsWeeklySchedule.getHoursWednesday())
            .add(mpsWeeklySchedule.getHoursThursday())
            .add(mpsWeeklySchedule.getHoursFriday())
            .add(mpsWeeklySchedule.getHoursSaturday())
            .add(mpsWeeklySchedule.getHoursSunday()));
  }

  @Override
  public BigDecimal getHoursForWeekDay(MpsWeeklySchedule mpsWeeklySchedule, DayOfWeek dayOfWeek) {
    BigDecimal hours = BigDecimal.ZERO;

    switch (dayOfWeek) {
      case MONDAY:
        hours = mpsWeeklySchedule.getHoursMonday();
        break;
      case TUESDAY:
        hours = mpsWeeklySchedule.getHoursTuesday();
        break;
      case WEDNESDAY:
        hours = mpsWeeklySchedule.getHoursWednesday();
        break;
      case THURSDAY:
        hours = mpsWeeklySchedule.getHoursThursday();
        break;
      case FRIDAY:
        hours = mpsWeeklySchedule.getHoursFriday();
        break;
      case SATURDAY:
        hours = mpsWeeklySchedule.getHoursSaturday();
        break;
      case SUNDAY:
        hours = mpsWeeklySchedule.getHoursSunday();
        break;
      default:
        break;
    }
    return hours;
  }
}
