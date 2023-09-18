package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproduction.exception.BusinessProductionExceptionMessage;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.auth.db.User;
import com.axelor.utils.date.DurationTool;
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
  protected TimesheetService timesheetService;

  @Inject
  public void TimesheetBusinessProductionServiceImpl(
      EmployeeService employeeService, TimesheetService timesheetService) {
    this.employeeService = employeeService;
    this.timesheetService = timesheetService;
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
          timesheetService.getOrCreateOpenTimesheet(
              employee, operationOrderDuration.getStartingDateTime().toLocalDate());
      TimesheetLine tsl = createTimesheetLine(timesheet, operationOrderDuration);

      timesheet.addTimesheetLineListItem(tsl);
      timesheet = timesheetService.save(timesheet);
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
            DurationTool.getSecondsDuration(
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
