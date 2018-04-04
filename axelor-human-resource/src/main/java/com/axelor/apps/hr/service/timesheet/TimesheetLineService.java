/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.timesheet;

import java.math.BigDecimal;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.exception.AxelorException;

public interface TimesheetLineService {

    /**
     * Compute duration from hours or to hours.
     *
     * @param timesheet a timesheet containing the time preference.
     * @param duration  the duration to be converted.
     * @param toHours   if we convert the duration to hours,
     *                  or if we convert from hours.
     * @return the computed duration.
     * @throws AxelorException
     */
    BigDecimal computeHoursDuration(Timesheet timesheet, BigDecimal duration, boolean toHours) throws AxelorException;

    /**
     * Compute duration from hours or to hours.
     *
     * @param timePref     a select containing the type of the duration.
     *                     Can be minute, hours or days.
     * @param duration     the duration to be converted
     * @param dailyWorkHrs The hours of work during a day.
     * @param toHours      if we convert the duration to hours, or if we convert from hours.
     * @return the computed duration.
     * @throws AxelorException
     */
    BigDecimal computeHoursDuration(String timePref, BigDecimal duration, BigDecimal dailyWorkHrs, boolean toHours) throws AxelorException;


}
