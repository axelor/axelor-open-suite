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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveRequestComputeDurationServiceImpl implements LeaveRequestComputeDurationService {

  protected final LeaveRequestComputeDayDurationService leaveRequestComputeDayDurationService;
  protected final LeaveRequestComputeHourDurationService leaveRequestComputeHourDurationService;

  @Inject
  public LeaveRequestComputeDurationServiceImpl(
      LeaveRequestComputeDayDurationService leaveRequestComputeDayDurationService,
      LeaveRequestComputeHourDurationService leaveRequestComputeHourDurationService) {
    this.leaveRequestComputeDayDurationService = leaveRequestComputeDayDurationService;
    this.leaveRequestComputeHourDurationService = leaveRequestComputeHourDurationService;
  }

  /**
   * Compute the duration of a given leave request but restricted inside a period.
   *
   * @param leave
   * @param fromDate the first date of the period
   * @param toDate the last date of the period
   * @return the computed duration in days
   * @throws AxelorException
   */
  @Override
  public BigDecimal computeDuration(LeaveRequest leave, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    LocalDateTime leaveFromDate = leave.getFromDateT();
    LocalDateTime leaveToDate = leave.getToDateT();

    int startOn = leave.getStartOnSelect();
    int endOn = leave.getEndOnSelect();

    LocalDateTime from = leaveFromDate;
    LocalDateTime to = leaveToDate;
    // if the leave starts before the beginning of the period,
    // we use the beginning date of the period.
    if (leaveFromDate.toLocalDate().isBefore(fromDate)) {
      from = fromDate.atStartOfDay();
      startOn = LeaveRequestRepository.SELECT_MORNING;
    }
    // if the leave ends before the end of the period,
    // we use the last date of the period.
    if (leaveToDate.toLocalDate().isAfter(toDate)) {
      to = toDate.atStartOfDay();
      endOn = LeaveRequestRepository.SELECT_AFTERNOON;
    }

    return computeDuration(leave, from, to, startOn, endOn);
  }

  /**
   * Compute the duration of a given leave request.
   *
   * @param leave
   * @return the computed duration in days
   * @throws AxelorException
   */
  @Override
  public BigDecimal computeDuration(LeaveRequest leave) throws AxelorException {
    return computeDuration(
        leave,
        leave.getFromDateT(),
        leave.getToDateT(),
        leave.getStartOnSelect(),
        leave.getEndOnSelect());
  }

  /**
   * Compute the duration of a given leave request. The duration can be in hours or in days,
   * depending of the leave reason of the leave.
   *
   * @param leave
   * @param from, the beginning of the leave request inside the period
   * @param to, the end of the leave request inside the period
   * @param startOn If the period starts in the morning or in the afternoon
   * @param endOn If the period ends in the morning or in the afternoon
   * @return the computed duration in days
   * @throws AxelorException
   */
  @Override
  public BigDecimal computeDuration(
      LeaveRequest leave, LocalDateTime from, LocalDateTime to, int startOn, int endOn)
      throws AxelorException {

    BigDecimal duration = BigDecimal.ZERO;

    if (from != null && to != null && leave.getLeaveReason() != null) {
      Employee employee = leave.getEmployee();

      switch (leave.getLeaveReason().getUnitSelect()) {
        case LeaveReasonRepository.UNIT_SELECT_DAYS:
          LocalDate fromDate = from.toLocalDate();
          LocalDate toDate = to.toLocalDate();
          duration =
              leaveRequestComputeDayDurationService.computeDurationInDays(
                  leave, employee, fromDate, toDate, startOn, endOn);
          break;

        case LeaveReasonRepository.UNIT_SELECT_HOURS:
          duration =
              leaveRequestComputeHourDurationService.computeDurationInHours(
                  leave, employee, from, to);
          break;

        default:
          throw new AxelorException(
              leave.getLeaveReason(),
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get(HumanResourceExceptionMessage.LEAVE_REASON_NO_UNIT),
              leave.getLeaveReason().getName());
      }
    }

    return duration.signum() != -1 ? duration : BigDecimal.ZERO;
  }
}
