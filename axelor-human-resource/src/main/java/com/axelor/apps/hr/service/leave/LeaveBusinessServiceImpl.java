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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.auth.db.User;
import jakarta.inject.Inject;
import java.util.Optional;

public class LeaveBusinessServiceImpl implements LeaveBusinessService {

  protected HRConfigService hrConfigService;
  protected EmployeeRepository employeeRepository;
  protected LeaveReasonRepository leaveReasonRepository;
  protected LeaveLineService leaveLineService;

  @Inject
  public LeaveBusinessServiceImpl(
      HRConfigService hrConfigService,
      EmployeeRepository employeeRepository,
      LeaveReasonRepository leaveReasonRepository,
      LeaveLineService leaveLineService) {
    this.hrConfigService = hrConfigService;
    this.employeeRepository = employeeRepository;
    this.leaveReasonRepository = leaveReasonRepository;
    this.leaveLineService = leaveLineService;
  }

  @Override
  public LeaveLine processLeaveReasonToJustify(LeaveRequest leave) throws AxelorException {
    Employee employee = leave.getEmployee();
    if (employee == null || !leave.getToJustifyLeaveReason()) {
      return null;
    }
    Company company = leave.getCompany();
    if (company == null) {
      Optional.ofNullable(employee.getUser()).map(User::getActiveCompany).orElse(null);
    }
    if (company == null) {
      return null;
    }
    LeaveReason leaveReason = hrConfigService.getLeaveReason(company.getHrConfig());
    return leaveLineService.addLeaveReasonOrCreateIt(
        employeeRepository.find(employee.getId()), leaveReasonRepository.find(leaveReason.getId()));
  }
}
