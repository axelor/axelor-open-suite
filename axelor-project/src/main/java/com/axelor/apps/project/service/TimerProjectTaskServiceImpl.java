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
package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
import com.axelor.apps.base.db.repo.TimerHistoryRepository;
import com.axelor.apps.base.db.repo.TimerRepository;
import com.axelor.apps.base.service.timer.AbstractTimerService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.db.User;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import java.time.Duration;
import java.time.LocalDateTime;
import javax.inject.Inject;
import javax.transaction.Transactional;

public class TimerProjectTaskServiceImpl extends AbstractTimerService
    implements TimerProjectTaskService {

  @Inject
  public TimerProjectTaskServiceImpl(
      TimerRepository timerRepository,
      TimerHistoryRepository timerHistoryRepository,
      UserService userService) {
    super(timerRepository, timerHistoryRepository, userService);
  }

  @Override
  public Timer find(Model model) {
    User user = userService.getUser();
    ProjectTask task = (ProjectTask) model;

    return task.getTimerList().stream()
        .filter(t -> t.getAssignedToUser() == user)
        .findFirst()
        .orElse(null);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public TimerHistory start(Model model, Timer timer, LocalDateTime dateTime)
      throws AxelorException {
    ProjectTask task = (ProjectTask) model;

    boolean isNewTimer = timer == null;
    timer = tryStartOrCreate(timer);
    if (isNewTimer) {
      task.addTimerListItem(timer);
    }

    TimerHistory history = new TimerHistory();
    history.setStartDateT(dateTime);
    history.setTimer(timer);
    timer.addTimerHistoryListItem(history);

    return timerHistoryRepository.save(history);
  }

  @Override
  public Timer find(ProjectTask task) {
    return find((Model) task);
  }

  @Override
  public TimerHistory start(ProjectTask task, LocalDateTime dateTime) throws AxelorException {
    Timer timer = find(task);
    return start(task, timer, dateTime);
  }

  @Override
  public TimerHistory stop(ProjectTask task, LocalDateTime dateTime) throws AxelorException {
    Timer timer = find(task);
    return stop(task, timer, dateTime);
  }

  @Override
  public void cancel(ProjectTask task) {
    Timer timer = find(task);
    cancel(timer);
  }

  @Override
  public Duration compute(ProjectTask task) {
    Duration total = Duration.ZERO;
    for (Timer timer : task.getTimerList()) {
      total = total.plus(compute(timer));
    }
    return total;
  }
}
