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
package com.axelor.apps.hr.service.timesheet.timer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.repo.TSTimerRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppTimesheet;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;

public class TimesheetTimerCreateServiceImpl implements TimesheetTimerCreateService {
  protected TSTimerRepository tsTimerRepository;
  protected AppHumanResourceService appHumanResourceService;
  protected TimesheetTimerService timesheetTimerService;

  @Inject
  public TimesheetTimerCreateServiceImpl(
      TSTimerRepository tsTimerRepository,
      AppHumanResourceService appHumanResourceService,
      TimesheetTimerService timesheetTimerService) {
    this.tsTimerRepository = tsTimerRepository;
    this.appHumanResourceService = appHumanResourceService;
    this.timesheetTimerService = timesheetTimerService;
  }

  @Override
  public TSTimer createOrUpdateTimer(
      Employee employee,
      Project project,
      ProjectTask projectTask,
      Product product,
      Long duration,
      String comment,
      LocalDateTime startDateTime)
      throws AxelorException {
    checkFields(employee, project, projectTask, product);
    AppTimesheet appTimesheet = appHumanResourceService.getAppTimesheet();
    boolean isMultipleTimer = appTimesheet.getIsMultipleTimerEnabled();
    TSTimer timer;

    if (isMultipleTimer) {
      return createTSTimer(
          employee, project, projectTask, product, duration, comment, startDateTime);
    } else {
      timer = timesheetTimerService.getCurrentTSTimer();
      if (timer == null) {
        return createTSTimer(
            employee, project, projectTask, product, duration, comment, startDateTime);
      }
      int statusSelect = timer.getStatusSelect();
      if (statusSelect == TSTimerRepository.STATUS_START
          || statusSelect == TSTimerRepository.STATUS_PAUSE) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMER_ALREADY_STARTED));
      }
      timesheetTimerService.resetTimer(timer);
      updateTimer(timer, employee, project, projectTask, product, duration, comment, startDateTime);
      updateDurationOnCreation(duration, timer);
    }

    return timer;
  }

  @Override
  public TSTimer createOrUpdateTimer(
      Project project,
      ProjectTask projectTask,
      Product product,
      Long duration,
      String comment,
      LocalDateTime startDateTime)
      throws AxelorException {
    Employee employee = null;
    User user = AuthUtils.getUser();
    if (user != null) {
      employee = user.getEmployee();
    }
    if (employee == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMER_USER_NO_EMPLOYEE));
    }
    return createOrUpdateTimer(
        employee, project, projectTask, product, duration, comment, startDateTime);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public TSTimer createTSTimer(
      Employee employee,
      Project project,
      ProjectTask projectTask,
      Product product,
      Long duration,
      String comment,
      LocalDateTime startDateTime)
      throws AxelorException {
    checkFields(employee, project, projectTask, product);
    TSTimer timer = new TSTimer();
    timer.setStatusSelect(TSTimerRepository.STATUS_DRAFT);
    updateTimer(timer, employee, project, projectTask, product, duration, comment, startDateTime);
    updateDurationOnCreation(duration, timer);
    tsTimerRepository.save(timer);
    timer.setName("Timer " + timer.getId());
    return timer;
  }

  @Transactional
  protected void updateDurationOnCreation(Long duration, TSTimer timer) {
    if (duration != null) {
      timer.setStatusSelect(TSTimerRepository.STATUS_PAUSE);
      timer.setDuration(duration);
    }
  }

  @Transactional
  @Override
  public TSTimer updateTimer(
      TSTimer timer,
      Employee employee,
      Project project,
      ProjectTask projectTask,
      Product product,
      Long duration,
      String comment,
      LocalDateTime startDateTime)
      throws AxelorException {
    AppTimesheet appTimesheet = appHumanResourceService.getAppTimesheet();

    if (duration != null && !appTimesheet.getEditModeTSTimer()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMER_TIMER_STOP_CONFIG_DISABLED));
    }

    checkRelation(project, projectTask, product);

    if (employee != null) {
      timer.setEmployee(employee);
    }
    if (project != null) {
      timer.setProject(project);
    }
    if (projectTask != null) {
      timer.setProjectTask(projectTask);
    }
    if (product != null) {
      timer.setProduct(product);
    }
    if (duration != null) {
      timer.setUpdatedDuration(duration);
    }
    if (StringUtils.notEmpty(comment)) {
      timer.setComments(comment);
    }
    if (startDateTime != null) {
      timer.setStartDateTime(startDateTime);
    }
    return timer;
  }

  protected void checkFields(
      Employee employee, Project project, ProjectTask projectTask, Product product)
      throws AxelorException {
    if (employee == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMER_EMPTY_EMPLOYEE));
    }

    if (product == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMER_EMPTY_ACTIVITY));
    }

    checkRelation(project, projectTask, product);
  }

  protected void checkRelation(Project project, ProjectTask projectTask, Product product)
      throws AxelorException {
    if ((project == null && projectTask != null) || (project != null && projectTask == null)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMER_EMPTY_PROJECT_OR_TASK));
    }

    if (project != null && projectTask != null && !project.equals(projectTask.getProject())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMER_PROJECT_TASK_INCONSISTENCY));
    }

    if (projectTask != null && !product.equals(projectTask.getProduct())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_TIMER_ACTIVITY_INCONSISTENCY));
    }
  }
}
