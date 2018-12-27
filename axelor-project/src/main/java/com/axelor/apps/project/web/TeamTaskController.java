/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.project.web;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.repo.TimerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.exception.IExceptionMessage;
import com.axelor.apps.project.service.TeamTaskService;
import com.axelor.apps.project.service.TimerTeamTaskService;
import com.axelor.apps.tool.ContextTool;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.team.db.TeamTask;
import java.time.Duration;

public class TeamTaskController {

  private static final String HIDDEN_ATTR = "hidden";
  private static final String TITLE_ATTR = "title";

  public void manageTimerButtons(ActionRequest request, ActionResponse response) {
    try {
      TeamTask task = request.getContext().asType(TeamTask.class);
      TimerTeamTaskService service = Beans.get(TimerTeamTaskService.class);

      Timer timer = service.find(task);

      boolean hideStart = false;
      boolean hideCancel = true;
      if (timer != null) {
        hideStart = timer.getStatusSelect() == TimerRepository.TIMER_STARTED;
        hideCancel = timer.getTimerHistoryList().isEmpty();
      }

      response.setAttr("startTimerBtn", HIDDEN_ATTR, hideStart);
      response.setAttr("stopTimerBtn", HIDDEN_ATTR, !hideStart);
      response.setAttr("cancelTimerBtn", HIDDEN_ATTR, hideCancel);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeTotalTimerDuration(ActionRequest request, ActionResponse response) {
    try {
      TeamTask task = request.getContext().asType(TeamTask.class);
      Duration duration = Beans.get(TimerTeamTaskService.class).compute(task);
      response.setValue("$_totalTimerDuration", duration.toMinutes() / 60F);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void startTimer(ActionRequest request, ActionResponse response) {
    try {
      TeamTask task = request.getContext().asType(TeamTask.class);
      Beans.get(TimerTeamTaskService.class)
          .start(task, Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void stopTimer(ActionRequest request, ActionResponse response) {
    try {
      TeamTask task = request.getContext().asType(TeamTask.class);
      Beans.get(TimerTeamTaskService.class)
          .stop(task, Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelTimer(ActionRequest request, ActionResponse response) {
    try {
      TeamTask task = request.getContext().asType(TeamTask.class);
      Beans.get(TimerTeamTaskService.class).cancel(task);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkTicketAssignment(ActionRequest request, ActionResponse response) {
    TeamTask task = request.getContext().asType(TeamTask.class);
    Project project = task.getProject();

    if (project != null && Beans.get(TeamTaskService.class).checkTicketAssignment(task, project)) {
      String msg =
          String.format(
              I18n.get(IExceptionMessage.PROJECT_TICKET_ASSIGNMENT),
              project.getCompany().getName());

      String title = ContextTool.formatLabel(msg, ContextTool.SPAN_CLASS_IMPORTANT, 75);

      response.setAttr("ticketAssignmentLabel", TITLE_ATTR, title);
      response.setAttr("ticketAssignmentLabel", HIDDEN_ATTR, false);
      response.setAttr("returnTicketButton", HIDDEN_ATTR, false);
    } else {
      response.setAttr("ticketAssignmentLabel", HIDDEN_ATTR, true);
      response.setAttr("returnTicketButton", HIDDEN_ATTR, true);
    }
  }
}
