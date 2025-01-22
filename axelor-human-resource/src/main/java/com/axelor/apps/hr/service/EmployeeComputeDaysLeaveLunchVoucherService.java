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
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeDurationService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeComputeDaysLeaveLunchVoucherService extends EmployeeComputeDaysLeaveService {
  protected LeaveRequestComputeDurationService leaveRequestComputeDurationService;

  @Inject
  public EmployeeComputeDaysLeaveLunchVoucherService(
      EmployeeService employeeService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveRequestComputeDurationService leaveRequestComputeDurationService) {
    super(employeeService, leaveRequestRepository);
    this.leaveRequestComputeDurationService = leaveRequestComputeDurationService;
  }

  /**
   * To compute duration for lunch vouchers, we need to count half-day leave as full days. Taking a
   * leave for a half-day will result in the employee not having the right to a lunch voucher for
   * this day.
   */
  @Override
  protected BigDecimal computeDuration(LeaveRequest leave, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {

    LocalDate leaveFromDate = leave.getFromDateT().toLocalDate();
    LocalDate leaveToDate = leave.getToDateT().toLocalDate();

    LocalDate finalToDate = leaveToDate.isAfter(toDate) ? toDate : leaveToDate;
    LocalDate finalFromDate = leaveFromDate.isBefore(fromDate) ? fromDate : leaveFromDate;

    return leaveRequestComputeDurationService.computeDuration(
        leave,
        finalFromDate.atStartOfDay(),
        finalToDate.atStartOfDay(),
        LeaveRequestRepository.SELECT_MORNING,
        LeaveRequestRepository.SELECT_AFTERNOON);
  }
}
