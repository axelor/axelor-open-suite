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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.leavereason.LeaveReasonService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestServiceImpl implements LeaveRequestService {

  protected LeaveLineRepository leaveLineRepository;
  protected LeaveRequestRepository leaveRequestRepository;

  protected AppBaseService appBaseService;
  protected LeaveReasonService leaveReasonService;

  @Inject
  public LeaveRequestServiceImpl(
      LeaveLineRepository leaveLineRepository,
      LeaveRequestRepository leaveRequestRepository,
      AppBaseService appBaseService,
      LeaveReasonService leaveReasonService) {

    this.leaveLineRepository = leaveLineRepository;
    this.leaveRequestRepository = leaveRequestRepository;
    this.appBaseService = appBaseService;
    this.leaveReasonService = leaveReasonService;
  }

  public List<LeaveRequest> getLeaves(Employee employee, LocalDate date) {
    List<LeaveRequest> leavesList = new ArrayList<>();
    List<LeaveRequest> leaves =
        leaveRequestRepository
            .all()
            .filter(
                "self.employee = :employee AND self.statusSelect IN (:awaitingValidation,:validated)")
            .bind("employee", employee)
            .bind("awaitingValidation", LeaveRequestRepository.STATUS_AWAITING_VALIDATION)
            .bind("validated", LeaveRequestRepository.STATUS_VALIDATED)
            .fetch();

    if (ObjectUtils.notEmpty(leaves)) {
      for (LeaveRequest leave : leaves) {
        LocalDate from = leave.getFromDateT().toLocalDate();
        LocalDate to = leave.getToDateT().toLocalDate();
        if ((from.isBefore(date) && to.isAfter(date)) || from.isEqual(date) || to.isEqual(date)) {
          leavesList.add(leave);
        }
      }
    }
    return leavesList;
  }

  @Override
  public boolean willHaveEnoughDays(LeaveRequest leaveRequest) throws AxelorException {
    LeaveLine leaveLine =
        leaveLineRepository
            .all()
            .filter("self.leaveReason = :leaveReason AND self.employee = :employee")
            .bind("leaveReason", leaveRequest.getLeaveReason())
            .bind("employee", leaveRequest.getEmployee())
            .fetchOne();
    if (leaveLine == null) {
      return leaveRequest.getLeaveReason() != null
          && leaveReasonService.isExceptionalDaysReason(leaveRequest.getLeaveReason());
    }

    BigDecimal num = getLeaveDaysToDate(leaveRequest);

    return leaveRequest.getDuration().compareTo(num) <= 0;
  }

  @Override
  public BigDecimal getLeaveDaysToDate(LeaveRequest leaveRequest) throws AxelorException {

    return getLeaveDaysToDate(
        leaveRequest.getToDateT(), leaveRequest.getEmployee(), leaveRequest.getLeaveReason());
  }

  @Override
  public BigDecimal getLeaveDaysToDate(
      LocalDateTime toDateT, Employee employee, LeaveReason leaveReason) throws AxelorException {
    LocalDateTime todayDate = appBaseService.getTodayDateTime().toLocalDateTime();

    if (todayDate == null || toDateT == null) {
      return BigDecimal.ZERO;
    }

    LeaveLine leaveLine =
        leaveLineRepository
            .all()
            .filter("self.leaveReason = :leaveReason AND self.employee = :employee")
            .bind("leaveReason", leaveReason)
            .bind("employee", employee)
            .fetchOne();

    if (leaveReason == null || leaveLine == null) {
      return BigDecimal.ZERO;
    }

    int leaveReasonTypeSelect = leaveReason.getLeaveReasonTypeSelect();

    int interval = getInterval(leaveReasonTypeSelect, toDateT, todayDate);
    WeeklyPlanning planning = employee.getWeeklyPlanning();
    if (planning == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_PLANNING),
          employee.getName());
    }
    return leaveLine
        .getQuantity()
        .add(
            planning
                .getLeaveCoef()
                .multiply(leaveReason.getDefaultDayNumberGain())
                .multiply(BigDecimal.valueOf(interval)));
  }

  protected int getInterval(
      int leaveReasonTypeSelect, LocalDateTime endDate, LocalDateTime todayDate) {
    int interval = 0;

    if (leaveReasonTypeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_MONTH) {
      interval =
          (endDate.getYear() - todayDate.getYear()) * 12
              + endDate.getMonthValue()
              - todayDate.getMonthValue();
    }

    if (leaveReasonTypeSelect == LeaveReasonRepository.TYPE_SELECT_EVERY_YEAR) {
      interval = endDate.getYear() - todayDate.getYear();
    }
    return interval;
  }

  @Override
  public String getLeaveCalendarDomain(User user) {

    StringBuilder domain = new StringBuilder("self.statusSelect = 3");
    Employee employee = user.getEmployee();

    if (employee == null || !employee.getHrManager()) {
      domain.append(
          " AND (self.employee.managerUser.id = :userId OR self.employee.user.id = :userId)");
    }

    return domain.toString();
  }

  @Override
  public boolean isLeaveDay(Employee employee, LocalDate date) {
    return ObjectUtils.notEmpty(getLeaves(employee, date));
  }
}
