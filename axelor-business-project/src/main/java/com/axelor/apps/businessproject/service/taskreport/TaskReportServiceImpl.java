package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.TaskReportRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskReportServiceImpl implements TaskReportService {

  private static final Logger log = LoggerFactory.getLogger(TaskReportServiceImpl.class);
  private static final String CUSTOM_PROJECT_TECHNICIAN_GROUP_CODE = "CUSTOM-PT";

  @Inject TimesheetRepository timesheetRepository;

  @Inject TimesheetLineRepository timesheetLineRepository;

  @Override
  public boolean checkIfAllTasksReported(TaskReport report) {

    TaskReport taskReport = fetchTaskReport(report);
    if (taskReport == null) {
      return false;
    }

    // Get unique reported task IDs, skip template tasks
    Set<Long> reportedTaskIds = getReportedTaskIds(taskReport);
    if (reportedTaskIds.isEmpty()) {
      return false;
    }

    // Get all task Ids for project to use in comparision. skip template taks
    Set<Long> allProjectTaskIds = getAllProjectTaskIds(taskReport);
    if (allProjectTaskIds.isEmpty()) {
      return false;
    }

    // Return true only if all project tasks have been reported
    boolean reportedAllTasks = reportedTaskIds.containsAll(allProjectTaskIds);

    log.info("all task reported ? {}", reportedAllTasks);
    return reportedAllTasks;
  }

  @Override
  public String getReportedTaskCount(TaskReport report) {

    TaskReport taskReport = fetchTaskReport(report);
    if (taskReport == null) {
      return "0/0";
    }

    return getReportedTaskIds(taskReport).size() + "/" + getAllProjectTaskIds(taskReport).size();
  }

  @Override
  public String buildTaskDomainFilter(TaskReport report, Long currentTaskId, User user) {

    TaskReport taskReport = fetchTaskReport(report);
    if (taskReport == null) {
      log.warn("TaskReport or Project is null, returning empty domain");
      return "self.id IN (0)";
    }

    Long projectId = taskReport.getProject().getId();
    String userGroupCode = user.getGroup() != null ? user.getGroup().getCode() : null;

    Set<Long> reportedTaskIds = getReportedTaskIds(taskReport);

    // when editng a task, the task being edited should not be filtered out
    if (currentTaskId != null) {
      reportedTaskIds.remove(currentTaskId);
    }

    // Build domain
    StringBuilder domain = new StringBuilder("self.project.id = ");
    domain.append(projectId);
    domain.append(" AND (self.isTemplate = false OR self.isTemplate IS NULL)");

    // Exclude already reported tasks
    if (!reportedTaskIds.isEmpty()) {
      String excludedIds =
          reportedTaskIds.stream().map(String::valueOf).collect(Collectors.joining(","));
      domain.append(" AND self.id NOT IN (").append(excludedIds).append(")");
    }

    // Filter by assigned user for project technician group
    if (CUSTOM_PROJECT_TECHNICIAN_GROUP_CODE.equals(userGroupCode)) {
      domain.append(" AND self.assignedTo.id = ").append(user.getId());
    }

    log.debug("Built task domain: {}", domain);
    return domain.toString();
  }

  /**
   * Creates or updates a timesheet line for the given task member report when a task report is
   * saved.
   */
  @Override
  public void createTimesheetLineFromTMR(TaskMemberReport report) {
    if (report == null) return;
    if (report.getEmployee() == null || report.getTask() == null) return;
    if (report.getStartTime() == null || report.getEndTime() == null) return;

    User user = report.getEmployee();
    Employee employee = user.getEmployee();
    ProjectTask task = report.getTask();

    LocalDate date = report.getStartTime().toLocalDate();

    // Find available timesheet for the employee
    Timesheet timesheet = findOrCreateMonthlyTimesheet(employee, date);

    // Find existing timesheet line
    TimesheetLine line =
        timesheetLineRepository
            .all()
            .filter(
                "self.timesheet = ?1 " + "AND self.projectTask = ?2 " + "AND self.date = ?3",
                timesheet,
                task,
                date)
            .fetchOne();

    boolean isNew = false;

    // Create a new timesheet line if not found
    if (line == null) {
      line = new TimesheetLine();
      line.setTimesheet(timesheet);
      line.setEmployee(employee);
      line.setProject(task.getProject());
      line.setProjectTask(task);
      line.setProduct(task.getProduct());
      line.setDate(date);
      line.setStartTime(report.getStartTime());
      line.setEndTime(report.getEndTime());
      line.setToInvoice(true);
      line.setIsAutomaticallyGenerated(true);
      isNew = true;
    }

    // TSLine change detection
    boolean hasChanged =
        !Objects.equals(line.getStartTime(), report.getStartTime())
            || !Objects.equals(line.getEndTime(), report.getEndTime())
            || !Objects.equals(line.getDuration(), report.getWorkHours())
            || !Objects.equals(line.getBreakTimeMinutes(), report.getBreakTimeMinutes())
            || !Objects.equals(line.getProduct(), task.getProduct());

    // save TSLine invalidation
    if (!isNew && Boolean.TRUE.equals(line.getIsValidated()) && hasChanged) {
      line.setIsValidated(false);
    }

    // Always update calculated / mutable fields
    line.setEmployee(employee);
    line.setProject(task.getProject());
    line.setProjectTask(task);
    line.setProduct(task.getProduct());
    line.setDate(date);
    line.setStartTime(report.getStartTime());
    line.setEndTime(report.getEndTime());

    if (report.getWorkHours() != null) {
      BigDecimal duration = report.getWorkHours();
      line.setDuration(duration);
      line.setHoursDuration(duration);
      line.setDurationForCustomer(duration);
    } else {
      line.setDuration(null);
      line.setHoursDuration(null);
      line.setDurationForCustomer(null);
    }

    if (report.getBreakTimeMinutes() != null) {
      line.setBreakTimeMinutes(report.getBreakTimeMinutes());
    } else {
      line.setBreakTimeMinutes(null);
    }

    timesheetLineRepository.save(line);

    if (isNew) {
      log.debug(
          "Created timesheet line: timesheet={} task={} date={} start={} end={}",
          timesheet.getId(),
          task.getId(),
          date,
          report.getStartTime(),
          report.getEndTime());
    } else {
      log.debug(
          "Updated timesheet line: timesheet={} task={} date={} start={} end={}",
          timesheet.getId(),
          task.getId(),
          date,
          report.getStartTime(),
          report.getEndTime());
    }
  }

  @Override
  public Timesheet findOrCreateMonthlyTimesheet(Employee employee, LocalDate date) {
    // Try to find existing timesheet covering the date
    Timesheet timesheet =
        timesheetRepository
            .all()
            .filter(
                "self.employee = ?1 AND ?2 BETWEEN self.fromDate AND self.toDate", employee, date)
            .fetchOne();

    if (timesheet != null) {
      log.info("Found timesheet: {}", timesheet);
      return timesheet;
    }

    // Create timesheet for the month of the given date
    LocalDate fromDate = date.withDayOfMonth(1);
    LocalDate toDate = date.withDayOfMonth(date.lengthOfMonth());

    timesheet = new Timesheet();
    Company company = employee.getUser().getActiveCompany();
    if (company == null) {
      throw new IllegalStateException(
          I18n.get("Cannot create timesheet without active company for the employee"));
    }
    timesheet.setCompany(company);
    timesheet.setEmployee(employee);
    timesheet.setFromDate(fromDate);
    timesheet.setToDate(toDate);
    timesheet.setTimeLoggingPreferenceSelect("Hours");
    timesheet.setStatusSelect(TimesheetRepository.STATUS_DRAFT);

    timesheetRepository.save(timesheet);
    log.info("Created timesheet for employee={} from={} to={}", employee.getId(), fromDate, toDate);

    return timesheet;
  }

  @Override
  public TaskReport getTaskReport(Project project) {
    if (project == null) {
      return null;
    }
    return Query.of(TaskReport.class)
        .filter("self.project.id = :projectId")
        .bind("projectId", project.getId())
        .fetchOne();
  }

  /** Validates report and fetch entity from repository */
  private TaskReport fetchTaskReport(TaskReport report) {
    if (report == null || report.getId() == null) {
      return null;
    }

    TaskReport taskReport = Beans.get(TaskReportRepository.class).find(report.getId());
    if (taskReport == null || taskReport.getProject() == null) {
      return null;
    }

    return taskReport;
  }

  /** Get the id of all reported task excluding template tasks */
  private Set<Long> getReportedTaskIds(TaskReport report) {
    List<TaskMemberReport> taskMemberReports =
        report != null ? report.getTaskMemberReports() : null;

    if (taskMemberReports == null || taskMemberReports.isEmpty()) {
      return Collections.emptySet();
    }

    Set<Long> reportedTaskIds =
        taskMemberReports.stream()
            .map(TaskMemberReport::getTask)
            .filter(Objects::nonNull)
            .filter(task -> !Boolean.TRUE.equals(task.getIsTemplate()))
            .map(ProjectTask::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    log.info("Found {} reported tasks", reportedTaskIds.size());
    return reportedTaskIds;
  }

  /** Get the Id of all project tasks without template tasks */
  private Set<Long> getAllProjectTaskIds(TaskReport report) {
    List<ProjectTask> projectTasks =
        report != null
            ? (report.getProject() != null ? report.getProject().getProjectTaskList() : null)
            : null;

    if (projectTasks == null || projectTasks.isEmpty()) {
      return Collections.emptySet();
    }

    Set<Long> allProjectTaskIds =
        projectTasks.stream()
            .filter(task -> !Boolean.TRUE.equals(task.getIsTemplate()))
            .map(ProjectTask::getId)
            .collect(Collectors.toSet());

    log.info("Found {} project tasks", allProjectTaskIds.size());

    return allProjectTaskIds;
  }

  @Override
  public void createTaskReport(Project project) {
    TaskReport report = new TaskReport();
    report.setProject(project);
    report.setCustomer(project.getClientPartner());
    report.setLocation(project.getCustomerAddress());
    Beans.get(TaskReportRepository.class).save(report);
  }
}
