package com.axelor.apps.businessproject.service.taskreport;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.TaskMemberReportRepository;
import com.axelor.apps.businessproject.service.statuschange.TaskStatusChangeService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskMemberReportCreateServiceImpl implements TaskMemberReportCreateService {

  private static final Logger log =
      LoggerFactory.getLogger(TaskMemberReportCreateServiceImpl.class);
  protected TaskReportService taskReportService;
  protected TaskMemberReportService taskMemberReportService;
  protected TaskStatusChangeService taskStatusChangeService;

  @Inject
  public TaskMemberReportCreateServiceImpl(
      TaskReportService taskReportService,
      TaskMemberReportService taskMemberReportService,
      TaskStatusChangeService taskStatusChangeService) {
    this.taskReportService = taskReportService;
    this.taskMemberReportService = taskMemberReportService;
    this.taskStatusChangeService = taskStatusChangeService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public TaskMemberReportCreationResult createTaskMemberReport(
      ProjectTask task,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Integer breakTimeMinutes,
      Boolean dirtAllowance)
      throws AxelorException {
    if (task == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("Null Task. A task is required to create a task member report"));
    }

    TaskReport taskReport = taskReportService.getTaskReport(task.getProject());
    if (taskReport == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("Could not find a TaskReport to attach the Task Member Report to"));
    }

    BigDecimal initialTaskMemberReportWorkHours = BigDecimal.ZERO;

    TaskMemberReport taskMemberReport = taskMemberReportService.getTaskMemberReport(task);
    boolean isNew = taskMemberReport == null;
    if (isNew) {
      log.debug("Creating new task member report");
      breakTimeMinutes = Optional.ofNullable(breakTimeMinutes).orElse(0);
      dirtAllowance = Optional.ofNullable(dirtAllowance).orElse(false);
      taskMemberReport = new TaskMemberReport();
    } else {
      breakTimeMinutes =
          Optional.ofNullable(breakTimeMinutes).orElse(taskMemberReport.getBreakTimeMinutes());
      dirtAllowance =
          Optional.ofNullable(dirtAllowance).orElse(taskMemberReport.getDirtAllowance());
      initialTaskMemberReportWorkHours =
          taskMemberReport.getWorkHours() != null
              ? taskMemberReport.getWorkHours()
              : BigDecimal.ZERO;
    }

    setTaskMemberReportInfo(
        task, taskReport, startTime, endTime, breakTimeMinutes, dirtAllowance, taskMemberReport);

    if (isNew) {
      if (taskReport.getTaskMemberReports() == null) {
        taskReport.setTaskMemberReports(new ArrayList<>());
      }
      taskReport.getTaskMemberReports().add(taskMemberReport);
      taskReport.setTotalWorkHours(
          taskReport.getTotalWorkHours().add(taskMemberReport.getWorkHours()));
    } else {
      BigDecimal totalWorkHours =
          taskReport.getTotalWorkHours().subtract(initialTaskMemberReportWorkHours);
      taskReport.setTotalWorkHours(totalWorkHours.add(taskMemberReport.getWorkHours()));
    }

    taskReport.setDirtAllowance(
        taskReport.getTaskMemberReports().stream()
            .anyMatch(tmr -> Boolean.TRUE.equals(tmr.getDirtAllowance())));
    taskReport.setReportedAllTasks(taskReportService.allTasksReported(taskReport));

    taskMemberReport = Beans.get(TaskMemberReportRepository.class).save(taskMemberReport);

    taskStatusChangeService.setTaskStatusFeedback(task);
    taskReportService.createTimesheetLineFromTMR(taskMemberReport);

    return new TaskMemberReportCreationResult(taskMemberReport, isNew);
  }

  protected void setTaskMemberReportInfo(
      ProjectTask task,
      TaskReport taskReport,
      LocalDateTime startTime,
      LocalDateTime endTime,
      int breakTimeInMinutes,
      boolean dirtAllowance,
      TaskMemberReport taskMemberReport)
      throws AxelorException {
    if (task.getAssignedTo() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get("Task '%s' has no assigned employee"),
          task.getFullName());
    }

    BigDecimal breakTimeInHours =
        BigDecimal.valueOf(breakTimeInMinutes / 60.0).setScale(2, RoundingMode.HALF_UP);
    BigDecimal workHours =
        taskMemberReportService.computeWorkHours(startTime, endTime, breakTimeInHours);

    taskMemberReport.setTask(task);
    taskMemberReport.setEmployee(task.getAssignedTo());
    taskMemberReport.setTaskCategory(task.getProjectTaskCategory());
    taskMemberReport.setStartTime(startTime);
    taskMemberReport.setEndTime(endTime);
    taskMemberReport.setBreakTimeMinutes(breakTimeInMinutes);
    taskMemberReport.setBreakTimeHours(breakTimeInHours);
    taskMemberReport.setWorkHours(workHours);
    taskMemberReport.setDirtAllowance(dirtAllowance);
    taskMemberReport.setTaskReport(taskReport);
  }
}
