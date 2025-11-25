/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.web.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetCreateService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineRemoveService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.hr.service.timesheet.TimesheetQueryService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetLineController {

  private static final String HOURS_DURATION_FIELD = "hoursDuration";
  private static final String DURATION_FIELD = "duration";
  private static final Logger log = LoggerFactory.getLogger(TimesheetLineController.class);

  @Inject protected ProjectTaskRepository projectTaskRepo;
  @Inject protected ProjectRepository projectRepo;

  public void setStoredDuration(ActionRequest request, ActionResponse response) {
    try {
      TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
      Timesheet timesheet =
          Beans.get(TimesheetLineService.class)
              .resolveTimesheet(request.getContext(), timesheetLine);

      TimesheetLineService timesheetLineService = Beans.get(TimesheetLineService.class);
      BigDecimal hoursDuration =
          timesheetLineService.computeHoursDuration(timesheet, timesheetLine.getDuration(), true);

      timesheetLineService.checkDailyLimit(timesheet, timesheetLine, hoursDuration);
      response.setValue(HOURS_DURATION_FIELD, hoursDuration);

    } catch (Exception e) {
      resetDurationFields(response);
      TraceBackService.trace(response, e);
    }
  }

  public void setDuration(ActionRequest request, ActionResponse response) {
    try {
      TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
      Timesheet timesheet =
          Beans.get(TimesheetLineService.class)
              .resolveTimesheet(request.getContext(), timesheetLine);

      BigDecimal duration =
          Beans.get(TimesheetLineService.class)
              .computeHoursDuration(timesheet, timesheetLine.getHoursDuration(), false);

      response.setValue(DURATION_FIELD, duration);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @Transactional
  public void updateToInvoice(ActionRequest request, ActionResponse response) {
    try {
      TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
      timesheetLine = Beans.get(TimesheetLineRepository.class).find(timesheetLine.getId());
      timesheetLine.setToInvoice(!timesheetLine.getToInvoice());
      Beans.get(TimesheetLineRepository.class).save(timesheetLine);
      response.setValue("toInvoice", timesheetLine.getToInvoice());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkDailyLimit(ActionRequest request, ActionResponse response) {
    try {
      TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
      Timesheet timesheet =
          Beans.get(TimesheetLineService.class)
              .resolveTimesheet(request.getContext(), timesheetLine);

      Beans.get(TimesheetLineService.class)
          .checkDailyLimit(timesheet, timesheetLine, timesheetLine.getHoursDuration());
    } catch (Exception e) {
      resetDurationFields(response);
      TraceBackService.trace(response, e);
    }
  }

  public void setProduct(ActionRequest request, ActionResponse response) {
    TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
    response.setAttr(
        "product", "value", Beans.get(TimesheetLineService.class).getDefaultProduct(timesheetLine));
  }

  public void setTimesheet(ActionRequest request, ActionResponse response) {
    try {
      TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
      Timesheet timesheet =
          Beans.get(TimesheetCreateService.class).getOrCreateTimesheet(timesheetLine);
      response.setValue("timesheet", timesheet);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setTimesheetDomain(ActionRequest request, ActionResponse response) {
    try {
      TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
      String idList = "0";

      if (timesheetLine != null) {
        List<Timesheet> timesheetList =
            Beans.get(TimesheetQueryService.class).getTimesheetQuery(timesheetLine).fetch();
        idList = StringHelper.getIdListString(timesheetList);
      }

      response.setAttr("timesheet", "domain", "self.id IN (" + idList + ")");
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void logTime(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();

    ProjectTask projectTask = Beans.get(TimesheetLineService.class).resolveProjectTask(context);
    Project project = Beans.get(TimesheetLineService.class).resolveProject(context, projectTask);
    Employee employee = AuthUtils.getUser().getEmployee();

    buildLogTimeResponse(response, project, projectTask, employee);
  }

  public void removeProjectTimeSheetLines(ActionRequest request, ActionResponse response) {
    List<Integer> projectTimeSheetLineIds = (List<Integer>) request.getContext().get("_ids");

    if (!ObjectUtils.isEmpty(projectTimeSheetLineIds)) {
      Beans.get(TimesheetLineRemoveService.class).removeTimesheetLines(projectTimeSheetLineIds);
    }

    response.setReload(true);
  }

  private void resetDurationFields(ActionResponse response) {
    response.setValue(DURATION_FIELD, 0);
    response.setValue(HOURS_DURATION_FIELD, 0);
  }

  private void buildLogTimeResponse(
      ActionResponse response, Project project, ProjectTask projectTask, Employee employee) {
    response.setValue("_project", project);
    response.setValue("_projectTask", projectTask);
    response.setValue("_employee", employee);
    response.setValue("project", project);
    response.setValue("projectTask", projectTask);
    response.setValue("employee", employee);

    response.setView(
        ActionView.define(I18n.get("Create Timesheet line"))
            .model(TimesheetLine.class.getName())
            .add("form", "timesheet-line-timesheet-project-task-form")
            .param("popup", "reload")
            .param("forceEdit", "true")
            .param("show-toolbar", "false")
            .param("popup-save", "true")
            .context("_project", project)
            .context("_projectTask", projectTask)
            .context("_employee", employee)
            .context("project", project)
            .context("projectTask", projectTask)
            .context("employee", employee)
            .map());
  }
}
