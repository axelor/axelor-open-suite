/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
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

  @Inject
  public LeaveRequestServiceImpl(
      LeaveLineRepository leaveLineRepository,
      LeaveRequestRepository leaveRequestRepository,
      AppBaseService appBaseService) {

    this.leaveLineRepository = leaveLineRepository;
    this.leaveRequestRepository = leaveRequestRepository;
    this.appBaseService = appBaseService;
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
  public boolean willHaveEnoughDays(LeaveRequest leaveRequest) {

    LocalDateTime todayDate = appBaseService.getTodayDateTime().toLocalDateTime();
    LocalDateTime beginDate = leaveRequest.getFromDateT();

    int interval =
        (beginDate.getYear() - todayDate.getYear()) * 12
            + beginDate.getMonthValue()
            - todayDate.getMonthValue();
    LeaveLine leaveLine =
        leaveLineRepository
            .all()
            .filter("self.leaveReason = :leaveReason AND self.employee = :employee")
            .bind("leaveReason", leaveRequest.getLeaveReason())
            .bind("employee", leaveRequest.getEmployee())
            .fetchOne();
    if (leaveLine == null) {
      if (leaveRequest.getLeaveReason() != null
          && !leaveRequest.getLeaveReason().getManageAccumulation()) {
        return true;
      }

      return false;
    }

    BigDecimal num =
        leaveLine
            .getQuantity()
            .add(
                leaveRequest
                    .getEmployee()
                    .getWeeklyPlanning()
                    .getLeaveCoef()
                    .multiply(leaveRequest.getLeaveReason().getDefaultDayNumberGain())
                    .multiply(BigDecimal.valueOf(interval)));

    return leaveRequest.getDuration().compareTo(num) <= 0;
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
