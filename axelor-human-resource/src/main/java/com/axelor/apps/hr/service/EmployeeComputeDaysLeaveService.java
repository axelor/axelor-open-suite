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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public abstract class EmployeeComputeDaysLeaveService {

  protected EmployeeService employeeService;
  protected LeaveRequestRepository leaveRequestRepository;

  protected EmployeeComputeDaysLeaveService(
      EmployeeService employeeService, LeaveRequestRepository leaveRequestRepository) {
    this.employeeService = employeeService;
    this.leaveRequestRepository = leaveRequestRepository;
  }

  public BigDecimal getDaysWorkedInPeriod(Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    return employeeService
        .getDaysWorksInPeriod(employee, fromDate, toDate)
        .subtract(computeDaysLeave(employee, fromDate, toDate));
  }

  protected BigDecimal computeDaysLeave(Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    List<LeaveRequest> leaveRequestList = getEmployeeDaysLeave(employee, fromDate, toDate);

    BigDecimal daysLeave = BigDecimal.ZERO;
    for (LeaveRequest leaveRequest : leaveRequestList) {
      daysLeave = daysLeave.add(this.computeDuration(leaveRequest, fromDate, toDate));
    }
    return daysLeave;
  }

  protected List<LeaveRequest> getEmployeeDaysLeave(
      Employee employee, LocalDate fromDate, LocalDate toDate) {
    return leaveRequestRepository
        .all()
        .filter(
            "self.employee = ?1 AND self.duration > 0"
                + " AND self.statusSelect = ?2"
                + " AND (self.fromDateT BETWEEN ?3 AND ?4 OR self.toDateT BETWEEN ?3 AND ?4 OR ?3 BETWEEN self.fromDateT"
                + " AND self.toDateT OR ?4 BETWEEN self.fromDateT AND self.toDateT)",
            employee,
            LeaveRequestRepository.STATUS_VALIDATED,
            fromDate,
            toDate)
        .fetch();
  }

  protected abstract BigDecimal computeDuration(
      LeaveRequest leaveRequest, LocalDate fromDate, LocalDate toDate) throws AxelorException;
}
