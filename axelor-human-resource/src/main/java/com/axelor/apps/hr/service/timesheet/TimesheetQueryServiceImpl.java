package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

public class TimesheetQueryServiceImpl implements TimesheetQueryService {

  protected TimesheetRepository timesheetRepository;

  @Inject
  public TimesheetQueryServiceImpl(TimesheetRepository timesheetRepository) {
    this.timesheetRepository = timesheetRepository;
  }

  @Override
  public Query<Timesheet> getTimesheetQuery(TimesheetLine timesheetLine) {
    return getTimesheetQuery(
        timesheetLine.getEmployee(),
        Optional.of(timesheetLine)
            .map(TimesheetLine::getProject)
            .map(Project::getCompany)
            .orElse(null),
        timesheetLine.getDate());
  }

  @Override
  public Query<Timesheet> getTimesheetQuery(Employee employee, Company company, LocalDate date) {
    return timesheetRepository
        .all()
        .filter(
            "self.employee = ?1 AND self.company = ?2 AND (self.statusSelect = 1 OR self.statusSelect = 2) AND ((?3 BETWEEN self.fromDate AND self.toDate) OR (self.toDate = null))",
            employee,
            company,
            date);
  }
}
