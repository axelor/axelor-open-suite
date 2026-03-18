/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.service.leave.LeaveRequestPlanningService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class LeaveRequestComputeLeaveHoursServiceImpl
    implements LeaveRequestComputeLeaveHoursService {

  protected final LeaveRequestComputeDurationService leaveRequestComputeDurationService;
  protected final LeaveRequestPlanningService leaveRequestPlanningService;
  protected final WeeklyPlanningService weeklyPlanningService;

  @Inject
  public LeaveRequestComputeLeaveHoursServiceImpl(
      LeaveRequestComputeDurationService leaveRequestComputeDurationService,
      LeaveRequestPlanningService leaveRequestPlanningService,
      WeeklyPlanningService weeklyPlanningService) {
    this.leaveRequestComputeDurationService = leaveRequestComputeDurationService;
    this.leaveRequestPlanningService = leaveRequestPlanningService;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  @Override
  public BigDecimal computeTotalLeaveHours(
      LocalDate date, BigDecimal dayValueInHours, List<LeaveRequest> leaveList)
      throws AxelorException {
    BigDecimal totalLeaveHours = BigDecimal.ZERO;
    for (LeaveRequest leave : leaveList) {
      BigDecimal leaveHours = leaveRequestComputeDurationService.computeDuration(leave, date, date);
      if (leave.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
        WeeklyPlanning weeklyPlanning =
            leaveRequestPlanningService.getWeeklyPlanning(leave, leave.getEmployee());
        BigDecimal dayValueInDays =
            BigDecimal.valueOf(
                weeklyPlanningService.getWorkingDayValueInDays(weeklyPlanning, date));
        if (dayValueInDays.signum() != 0) {
          leaveHours =
              leaveHours.divide(dayValueInDays, 2, RoundingMode.HALF_UP).multiply(dayValueInHours);
        }
      }
      totalLeaveHours = totalLeaveHours.add(leaveHours);
    }
    return totalLeaveHours;
  }
}
