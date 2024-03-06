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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class TimesheetPeriodComputationServiceImpl implements TimesheetPeriodComputationService {
  protected TimesheetRepository timesheetRepository;

  @Inject
  public TimesheetPeriodComputationServiceImpl(TimesheetRepository timesheetRepository) {
    this.timesheetRepository = timesheetRepository;
  }

  @Transactional
  @Override
  public void setComputedPeriodTotal(Timesheet timesheet) {
    timesheet.setPeriodTotal(computePeriodTotal(timesheet));
    timesheetRepository.save(timesheet);
  }

  @Override
  public BigDecimal computePeriodTotal(Timesheet timesheet) {
    BigDecimal periodTotal = BigDecimal.ZERO;

    List<TimesheetLine> timesheetLines = timesheet.getTimesheetLineList();

    if (timesheetLines != null) {
      BigDecimal periodTotalTemp;
      for (TimesheetLine timesheetLine : timesheetLines) {
        if (timesheetLine != null) {
          periodTotalTemp = timesheetLine.getHoursDuration();
          if (periodTotalTemp != null) {
            periodTotal = periodTotal.add(periodTotalTemp);
          }
        }
      }
    }

    return periodTotal;
  }
}
