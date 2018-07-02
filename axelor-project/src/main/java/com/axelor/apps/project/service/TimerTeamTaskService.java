package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
import com.axelor.exception.AxelorException;
import com.axelor.team.db.TeamTask;
import java.time.Duration;
import java.time.LocalDateTime;

public interface TimerTeamTaskService {

  Timer find(TeamTask task);

  TimerHistory start(TeamTask task, LocalDateTime dateTime) throws AxelorException;

  TimerHistory stop(TeamTask task, LocalDateTime dateTime) throws AxelorException;

  Duration compute(TeamTask task);
}
