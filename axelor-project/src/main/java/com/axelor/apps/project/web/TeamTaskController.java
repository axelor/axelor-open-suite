/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.apps.project.service.TimerTeamTaskService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.team.db.TeamTask;
import java.time.Duration;

public class TeamTaskController {

  private static final String HIDDEN_ATTR = "hidden";

  public void manageTimerButtons(ActionRequest request, ActionResponse response) {
    try {
      TeamTask task = request.getContext().asType(TeamTask.class);
      TimerTeamTaskService service = Beans.get(TimerTeamTaskService.class);
      if (task.getId() == null) {
        return;
      }
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
      if (task.getId() == null) {
        return;
      }
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
}
