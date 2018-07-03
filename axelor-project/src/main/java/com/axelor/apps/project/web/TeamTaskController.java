package com.axelor.apps.project.web;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerState;
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

      Timer timer = service.find(task);

      boolean hideStart = false;
      boolean hideCancel = true;
      if (timer != null) {
        hideStart = timer.getState() == TimerState.STARTED;
        hideCancel = timer.getTimerHistoryList().isEmpty();
      }

      response.setAttr("btnStartTimer", HIDDEN_ATTR, hideStart);
      response.setAttr("btnStopTimer", HIDDEN_ATTR, !hideStart);
      response.setAttr("btnCancelTimer", HIDDEN_ATTR, hideCancel);
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
}
