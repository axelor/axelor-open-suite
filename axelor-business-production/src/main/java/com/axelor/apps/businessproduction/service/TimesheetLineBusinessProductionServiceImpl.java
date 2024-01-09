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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproduction.exception.BusinessProductionExceptionMessage;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.timesheet.TimesheetFetchService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.auth.db.User;
import com.axelor.utils.helpers.date.DurationHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public class TimesheetLineBusinessProductionServiceImpl
    implements TimesheetLineBusinessProductionService {

  protected EmployeeService employeeService;
  protected TimesheetFetchService timesheetFetchService;
  protected TimesheetRepository timesheetRepository;

  @Inject
  public void TimesheetBusinessProductionServiceImpl(
      EmployeeService employeeService,
      TimesheetFetchService timesheetFetchService,
      TimesheetRepository timesheetRepository) {
    this.employeeService = employeeService;
    this.timesheetFetchService = timesheetFetchService;
    this.timesheetRepository = timesheetRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public Optional<TimesheetLine> createTimesheetLine(OperationOrderDuration operationOrderDuration)
      throws AxelorException {

    User user = operationOrderDuration.getStartedBy();

    if (user.getEmployee() == null) {
      return Optional.empty();
    }

    Employee employee = employeeService.getEmployee(user);

    if (employee.getTimesheetImputationSelect() == EmployeeRepository.TIMESHEET_MANUF_ORDER
        && operationOrderDuration.getStartingDateTime() != null
        && operationOrderDuration.getStoppingDateTime() != null) {

      Timesheet timesheet =
          timesheetFetchService.getOrCreateOpenTimesheet(
              employee, operationOrderDuration.getStartingDateTime().toLocalDate());
      TimesheetLine tsl = createTimesheetLine(timesheet, operationOrderDuration);

      timesheet.addTimesheetLineListItem(tsl);
      timesheet = timesheetRepository.save(timesheet);
      return Optional.ofNullable(tsl);
    }

    return Optional.empty();
  }

  protected TimesheetLine createTimesheetLine(
      Timesheet timesheet, OperationOrderDuration operationOrderDuration) throws AxelorException {

    Objects.requireNonNull(timesheet);
    Objects.requireNonNull(operationOrderDuration);
    Objects.requireNonNull(operationOrderDuration.getStoppingDateTime());
    Objects.requireNonNull(operationOrderDuration.getStartingDateTime());

    TimesheetLine tsl = new TimesheetLine();

    tsl.setOperationOrder(operationOrderDuration.getOperationOrder());
    tsl.setManufOrder(
        Optional.ofNullable(operationOrderDuration.getOperationOrder())
            .map(OperationOrder::getManufOrder)
            .orElse(null));

    tsl.setDuration(
        computeDuration(
            timesheet,
            DurationHelper.getSecondsDuration(
                Duration.between(
                    operationOrderDuration.getStartingDateTime(),
                    operationOrderDuration.getStoppingDateTime()))));

    tsl.setDate(operationOrderDuration.getStartingDateTime().toLocalDate());
    tsl.setEmployee(timesheet.getEmployee());

    return tsl;
  }

  @Override
  public BigDecimal computeDuration(Timesheet timesheet, long durationInSeconds)
      throws AxelorException {
    Objects.requireNonNull(timesheet);

    switch (timesheet.getTimeLoggingPreferenceSelect()) {
      case EmployeeRepository.TIME_PREFERENCE_DAYS:
        return BigDecimal.valueOf((double) durationInSeconds / (double) 86400)
            .setScale(2, RoundingMode.HALF_UP);
      case EmployeeRepository.TIME_PREFERENCE_HOURS:
        return BigDecimal.valueOf((double) durationInSeconds / (double) 3600)
            .setScale(2, RoundingMode.HALF_UP);
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        return BigDecimal.valueOf((double) durationInSeconds / (double) 60)
            .setScale(2, RoundingMode.HALF_UP);

      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            BusinessProductionExceptionMessage.EMPLOYEE_TIME_PREFERENCE_INVALID_VALUE);
    }
  }
}
