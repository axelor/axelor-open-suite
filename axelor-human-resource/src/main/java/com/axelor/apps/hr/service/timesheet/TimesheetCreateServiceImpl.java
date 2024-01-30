package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.db.User;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimesheetCreateServiceImpl implements TimesheetCreateService {

  protected UserHrService userHrService;
  protected ProjectRepository projectRepository;
  protected TimesheetLineService timesheetLineService;

  @Inject
  public TimesheetCreateServiceImpl(
      UserHrService userHrService,
      ProjectRepository projectRepository,
      TimesheetLineService timesheetLineService) {
    this.userHrService = userHrService;
    this.projectRepository = projectRepository;
    this.timesheetLineService = timesheetLineService;
  }

  @Override
  public Timesheet createTimesheet(Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    Timesheet timesheet = new Timesheet();
    timesheet.setEmployee(employee);

    Company company = null;
    if (employee != null) {
      if (employee.getMainEmploymentContract() != null) {
        company = employee.getMainEmploymentContract().getPayCompany();
      } else if (employee.getUser() != null) {
        company = employee.getUser().getActiveCompany();
      }
    }

    String timeLoggingPreferenceSelect =
        employee == null ? null : employee.getTimeLoggingPreferenceSelect();
    timesheet.setTimeLoggingPreferenceSelect(timeLoggingPreferenceSelect);
    timesheet.setCompany(company);
    timesheet.setFromDate(fromDate);
    timesheet.setStatusSelect(TimesheetRepository.STATUS_DRAFT);

    return timesheet;
  }

  @Override
  public List<Map<String, Object>> createDefaultLines(Timesheet timesheet) {

    List<Map<String, Object>> lines = new ArrayList<>();
    User user = timesheet.getEmployee().getUser();
    if (user == null || timesheet.getFromDate() == null) {
      return lines;
    }

    Product product = userHrService.getTimesheetProduct(timesheet.getEmployee());

    if (product == null) {
      return lines;
    }

    List<Project> projects =
        projectRepository
            .all()
            .filter(
                "self.membersUserSet.id = ?1 and "
                    + "self.imputable = true "
                    + "and self.projectStatus.isCompleted = false "
                    + "and self.isShowTimeSpent = true",
                user.getId())
            .fetch();

    for (Project project : projects) {
      TimesheetLine line =
          timesheetLineService.createTimesheetLine(
              project,
              product,
              timesheet.getEmployee(),
              timesheet.getFromDate(),
              timesheet,
              new BigDecimal(0),
              null);
      lines.add(Mapper.toMap(line));
    }

    return lines;
  }
}
