package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class LeaveValueProrataServiceImpl implements LeaveValueProrataService {
  protected EmployeeService employeeService;

  @Inject
  public LeaveValueProrataServiceImpl(EmployeeService employeeService) {
    this.employeeService = employeeService;
  }

  @Override
  public BigDecimal getProratedValue(
      BigDecimal value,
      LeaveReason leaveReason,
      Employee employee,
      LocalDate fromDate,
      LocalDate toDate)
      throws AxelorException {
    BigDecimal ratio = getRatioForProratedValue(leaveReason, employee, fromDate, toDate);
    return value
        .multiply(ratio)
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  protected BigDecimal getRatioForProratedValue(
      LeaveReason leaveReason, Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    LocalDate employeeHireDate = employee.getHireDate();
    LocalDate employeeLeavingDate = employee.getLeavingDate();

    if (!leaveReason.getIsDaysAddedProrated()
        || (!LocalDateHelper.isBetween(fromDate, toDate, employeeHireDate)
            && employeeHireDate.isBefore(fromDate))) {
      return BigDecimal.ONE;
    }

    if (!LocalDateHelper.isBetween(fromDate, toDate, employeeHireDate)
        && employeeHireDate.isAfter(toDate)) {
      return BigDecimal.ZERO;
    }

    BigDecimal ratio = BigDecimal.ONE;
    if (LocalDateHelper.isBetween(fromDate, toDate, employeeHireDate)) {
      BigDecimal employeeWorkDays =
          employeeService.getDaysWorksInPeriod(employee, employeeHireDate, toDate);
      if (employeeLeavingDate != null && employeeLeavingDate.isBefore(toDate)) {
        employeeWorkDays =
            employeeService.getDaysWorksInPeriod(employee, employeeHireDate, employeeLeavingDate);
      }

      BigDecimal workDays = employeeService.getDaysWorksInPeriod(employee, fromDate, toDate);
      ratio =
          employeeWorkDays.divide(
              workDays, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
    }

    return ratio;
  }
}
