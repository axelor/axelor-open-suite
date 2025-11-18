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
package com.axelor.apps.hr.service.leave.compute;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LeaveRequestComputeCalendarDayServiceImpl
    implements LeaveRequestComputeCalendarDayService {

  @Override
  public BigDecimal computeDurationInCalendarDays(
      LocalDate fromDate, LocalDate toDate, int startOn, int endOn) throws AxelorException {

    BigDecimal duration = BigDecimal.ZERO;

    // If the leave request is only for 1 day
    if (fromDate.isEqual(toDate)) {
      if (startOn == endOn) {
        if (startOn == LeaveRequestRepository.SELECT_MORNING) {
          duration = duration.add(getDayValueWithSelect(true, false));
        } else {
          duration = duration.add(getDayValueWithSelect(false, true));
        }
      } else {
        duration = duration.add(getDayValueWithSelect(true, true));
      }

      // Else if it's on several days
    } else {
      duration =
          duration.add(
              getDayValueWithSelect(startOn == LeaveRequestRepository.SELECT_MORNING, true));

      LocalDate itDate = fromDate.plusDays(1);
      while (!itDate.isEqual(toDate) && !itDate.isAfter(toDate)) {
        duration = duration.add(getDayValueWithSelect(true, true));
        itDate = itDate.plusDays(1);
      }

      duration =
          duration.add(
              getDayValueWithSelect(true, endOn == LeaveRequestRepository.SELECT_AFTERNOON));
    }

    return duration;
  }

  protected BigDecimal getDayValueWithSelect(boolean morning, boolean afternoon) {
    BigDecimal value = BigDecimal.ZERO;
    if (morning) {
      value = value.add(BigDecimal.valueOf(0.5));
    }
    if (afternoon) {
      value = value.add(BigDecimal.valueOf(0.5));
    }
    return value;
  }
}
