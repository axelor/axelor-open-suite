/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeComputeDaysLeaveLunchVoucherService extends EmployeeComputeDaysLeaveService {
  protected LeaveService leaveService;

  @Inject
  public EmployeeComputeDaysLeaveLunchVoucherService(
      EmployeeService employeeService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveService leaveService) {
    super(employeeService, leaveRequestRepository);
    this.leaveService = leaveService;
  }

  /**
   * To compute duration for lunch vouchers, we need to count half-day leave as full days. Taking a
   * leave for a half-day will result in the employee not having the right to a lunch voucher for
   * this day.
   */
  @Override
  protected BigDecimal computeDuration(LeaveRequest leave, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    return leaveService.computeDuration(
        leave,
        leave.getFromDateT(),
        leave.getToDateT(),
        LeaveRequestRepository.SELECT_MORNING,
        LeaveRequestRepository.SELECT_AFTERNOON);
  }
}
