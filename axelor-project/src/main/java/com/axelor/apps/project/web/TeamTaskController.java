package com.axelor.apps.project.web;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.service.TimerTeamTaskService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.team.db.TeamTask;
import java.time.Duration;

public class TeamTaskController {
  public void manageTimerButtons(ActionRequest request, ActionResponse response) {
    try {
      TeamTask task = request.getContext().asType(TeamTask.class);

      Timer timer = Beans.get(TimerTeamTaskService.class).find(task);
      boolean hasTimer = timer != null;
      response.setAttr("btnStartTimer", "hidden", hasTimer);
      response.setAttr("btnStopTimer", "hidden", !hasTimer);
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
}
