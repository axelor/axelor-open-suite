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
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LeaveRequestComputeLeaveHoursServiceImpl
    implements LeaveRequestComputeLeaveHoursService {

  protected final LeaveRequestComputeDurationService leaveRequestComputeDurationService;

  @Inject
  public LeaveRequestComputeLeaveHoursServiceImpl(
      LeaveRequestComputeDurationService leaveRequestComputeDurationService) {
    this.leaveRequestComputeDurationService = leaveRequestComputeDurationService;
  }

  @Override
  public BigDecimal computeTotalLeaveHours(
      LocalDate date, BigDecimal dayValueInHours, List<LeaveRequest> leaveList)
      throws AxelorException {
    BigDecimal totalLeaveHours = BigDecimal.ZERO;
    for (LeaveRequest leave : leaveList) {
      BigDecimal leaveHours = leaveRequestComputeDurationService.computeDuration(leave, date, date);
      if (leave.getLeaveReason().getUnitSelect() == LeaveReasonRepository.UNIT_SELECT_DAYS) {
        leaveHours = leaveHours.multiply(dayValueInHours);
      }
      totalLeaveHours = totalLeaveHours.add(leaveHours);
    }
    return totalLeaveHours;
  }
}
