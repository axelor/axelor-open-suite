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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.timesheet.TimesheetFetchService;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ManufacturingOperationDuration;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.util.Objects;

public class ManufacturingOperationBusinessProductionCheckServiceImpl
    implements ManufacturingOperationBusinessProductionCheckService {

  protected EmployeeService employeeService;
  protected TimesheetFetchService timesheetFetchService;

  @Inject
  public ManufacturingOperationBusinessProductionCheckServiceImpl(
      EmployeeService employeeService, TimesheetFetchService timesheetFetchService) {
    this.employeeService = employeeService;
    this.timesheetFetchService = timesheetFetchService;
  }

  @Override
  public boolean workingUsersHaveEmployee(ManufacturingOperation manufacturingOperation) {
    Objects.requireNonNull(manufacturingOperation);

    return manufacturingOperation.getManufacturingOperationDurationList().stream()
        .filter(ood -> ood.getStoppingDateTime() == null)
        .map(ManufacturingOperationDuration::getStartedBy)
        .allMatch(user -> user.getEmployee() != null);
  }

  @Override
  public boolean workingUsersHaveTSImputationSelect(
      ManufacturingOperation manufacturingOperation, int tsImputationSelect) {
    Objects.requireNonNull(manufacturingOperation);

    return manufacturingOperation.getManufacturingOperationDurationList().stream()
        .filter(ood -> ood.getStoppingDateTime() == null)
        .map(ManufacturingOperationDuration::getStartedBy)
        .map(
            user -> {
              try {
                return employeeService.getEmployee(user);
              } catch (Exception e) {
                return null;
              }
            })
        .filter(employee -> employee != null)
        .allMatch(
            employee ->
                employee != null && employee.getTimesheetImputationSelect() == tsImputationSelect);
  }

  @Override
  public boolean workingUsersHaveCorrectTimeLoggingPref(
      ManufacturingOperation manufacturingOperation) {
    Objects.requireNonNull(manufacturingOperation);

    return manufacturingOperation.getManufacturingOperationDurationList().stream()
        .filter(ood -> ood.getStoppingDateTime() == null)
        .map(ManufacturingOperationDuration::getStartedBy)
        .map(
            user -> {
              try {
                return employeeService.getEmployee(user);
              } catch (Exception e) {
                return null;
              }
            })
        .filter(employee -> employee != null)
        .allMatch(
            employee ->
                employee != null
                    && (EmployeeRepository.TIME_PREFERENCE_HOURS.equals(
                            employee.getTimeLoggingPreferenceSelect())
                        || EmployeeRepository.TIME_PREFERENCE_MINUTES.equals(
                            employee.getTimeLoggingPreferenceSelect())));
  }

  @Override
  public boolean workingUsersHaveTSTimeLoggingPrefMatching(
      ManufacturingOperation manufacturingOperation) {
    Objects.requireNonNull(manufacturingOperation);

    return manufacturingOperation.getManufacturingOperationDurationList().stream()
        .filter(ood -> ood.getStoppingDateTime() == null)
        .allMatch(
            manufacturingOperationDuration -> matchWithTimesheet(manufacturingOperationDuration));
  }

  protected boolean matchWithTimesheet(
      ManufacturingOperationDuration manufacturingOperationDuration) {

    User user = manufacturingOperationDuration.getStartedBy();

    if (user.getEmployee() == null) {
      // Returns true because we don't want to trigger alert if employee does not exist.
      return true;
    }

    try {

      Employee employee = employeeService.getEmployee(user);

      // If it is null then no problem with matching since a new one will be created
      Timesheet timesheet =
          timesheetFetchService.getDraftTimesheet(
              employee, manufacturingOperationDuration.getStartingDateTime().toLocalDate());

      if (timesheet == null) {
        return true;
      }

      return timesheet
          .getTimeLoggingPreferenceSelect()
          .equals(employee.getTimeLoggingPreferenceSelect());
    } catch (Exception e) {
      TraceBackService.trace(e);
      return false;
    }
  }
}
