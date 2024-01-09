package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Objects;

public class TimesheetFetchServiceImpl implements TimesheetFetchService {

  protected EmployeeService employeeService;
  protected AppHumanResourceService appHumanResourceService;
  protected TimesheetRepository timesheetRepository;
  protected TimesheetCreateService timesheetCreateService;

  @Inject
  public TimesheetFetchServiceImpl(
      EmployeeService employeeService,
      AppHumanResourceService appHumanResourceService,
      TimesheetRepository timesheetRepository,
      TimesheetCreateService timesheetCreateService) {
    this.employeeService = employeeService;
    this.appHumanResourceService = appHumanResourceService;
    this.timesheetRepository = timesheetRepository;
    this.timesheetCreateService = timesheetCreateService;
  }

  @Override
  public Timesheet getDraftTimesheet(Employee employee, LocalDate date) {

    Objects.requireNonNull(employee);
    Objects.requireNonNull(date);

    Timesheet timesheet =
        timesheetRepository
            .all()
            .filter(
                "self.statusSelect = :timesheetStatus AND self.employee = :employee AND self.fromDate <= :date AND self.isCompleted = false")
            .bind("timesheetStatus", TimesheetRepository.STATUS_DRAFT)
            .bind("employee", employee)
            .bind("date", date)
            .order("-id")
            .fetchOne();
    if (timesheet != null) {
      return timesheet;
    } else {
      return null;
    }
  }

  @Override
  public Timesheet getCurrentOrCreateTimesheet() throws AxelorException {

    return getOrCreateOpenTimesheet(
        employeeService.getEmployee(AuthUtils.getUser()),
        appHumanResourceService.getTodayDateTime().toLocalDate());
  }

  @Override
  public Timesheet getOrCreateOpenTimesheet(Employee employee, LocalDate date)
      throws AxelorException {
    Timesheet timesheet = getDraftTimesheet(employee, date);
    if (timesheet == null) {

      timesheet = timesheetCreateService.createTimesheet(employee, date, null);
    }
    return timesheet;
  }
}
