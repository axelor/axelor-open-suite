package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TimesheetLineCreateServiceImpl implements TimesheetLineCreateService {
  protected TimesheetLineService timesheetLineService;
  protected TimesheetLineRepository timesheetLineRepository;

  @Inject
  public TimesheetLineCreateServiceImpl(
      TimesheetLineService timesheetLineService, TimesheetLineRepository timesheetLineRepository) {
    this.timesheetLineService = timesheetLineService;
    this.timesheetLineRepository = timesheetLineRepository;
  }

  @Transactional
  @Override
  public TimesheetLine createTimesheetLine(
      Project project,
      ProjectTask projectTask,
      Product product,
      LocalDate date,
      Timesheet timesheet,
      BigDecimal duration,
      String comments)
      throws AxelorException {
    checkDate(date, timesheet);
    Employee employee = AuthUtils.getUser().getEmployee();
    if (employee == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          HumanResourceExceptionMessage.LEAVE_USER_EMPLOYEE);
    }
    TimesheetLine timesheetLine =
        timesheetLineService.createTimesheetLine(
            project, product, employee, date, timesheet, duration, comments);
    timesheetLine.setProjectTask(projectTask);
    return timesheetLineRepository.save(timesheetLine);
  }

  protected void checkDate(LocalDate date, Timesheet timesheet) throws AxelorException {
    LocalDate fromDate = timesheet.getFromDate();
    LocalDate toDate = timesheet.getToDate();
    if (date != null
        && ((fromDate != null && date.isBefore(fromDate))
            || (toDate != null && date.isAfter(toDate)))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_LINE_INVALID_DATE));
    }
  }
}
