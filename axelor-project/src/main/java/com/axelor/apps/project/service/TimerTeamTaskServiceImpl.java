package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
import com.axelor.apps.base.db.TimerState;
import com.axelor.apps.base.db.repo.TimerHistoryRepository;
import com.axelor.apps.base.db.repo.TimerRepository;
import com.axelor.apps.base.service.timer.AbstractTimerService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.project.exception.IExceptionMessage;
import com.axelor.auth.db.User;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.team.db.TeamTask;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;

public class TimerTeamTaskServiceImpl extends AbstractTimerService implements TimerTeamTaskService {

  protected UserService userService;

  @Inject
  public TimerTeamTaskServiceImpl(
      TimerRepository timerRepository,
      TimerHistoryRepository timerHistoryRepository,
      UserService userService) {
    super(timerRepository, timerHistoryRepository);
    this.userService = userService;
  }

  @Override
  public Timer find(Model model) {
    User user = userService.getUser();
    TeamTask task = (TeamTask) model;

    return task.getTimerList()
        .stream()
        .filter(t -> t.getAssignedTo() == user && t.getState() == TimerState.STARTED)
        .findFirst()
        .orElse(null);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class, RuntimeException.class})
  public TimerHistory start(Model model, Timer timer, LocalDateTime dateTime)
      throws AxelorException {
    TeamTask task = (TeamTask) model;

    if (timer == null) {
      timer = new Timer();
      timer.setAssignedTo(userService.getUser());
      task.addTimerListItem(timer);
      timerRepository.save(timer);
    } else if (timer.getState().equals(TimerState.STARTED)) {
      throw new AxelorException(
          TraceBackRepository.TYPE_FUNCTIONNAL,
          I18n.get(IExceptionMessage.TEAM_TASK_TIMER_IS_NOT_STOPPED));
    }
    timer.setState(TimerState.STARTED);

    TimerHistory history = new TimerHistory();
    history.setStartDate(dateTime);
    history.setTimer(timer);
    timer.addTimerHistoryListItem(history);

    return timerHistoryRepository.save(history);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class, RuntimeException.class})
  public TimerHistory stop(Model model, Timer timer, LocalDateTime dateTime)
      throws AxelorException {
    Preconditions.checkNotNull(timer, I18n.get(IExceptionMessage.TEAM_TASK_TIMER_IS_NOT_STARTED));

    TimerHistory last = timerHistoryRepository.findByTimer(timer).order("-startDate").fetchOne();
    if (last == null) {
      throw new AxelorException(
          TraceBackRepository.TYPE_FUNCTIONNAL,
          I18n.get(IExceptionMessage.TEAM_TASK_TIMER_IS_NOT_STARTED));
    }

    last.setEndDate(dateTime);
    timer.setState(TimerState.STOPPED);

    return last;
  }

  @Override
  public Timer find(TeamTask task) {
    return find((Model) task);
  }

  @Override
  public TimerHistory start(TeamTask task, LocalDateTime dateTime) throws AxelorException {
    Timer timer = find(task);
    return start(task, timer, dateTime);
  }

  @Override
  public TimerHistory stop(TeamTask task, LocalDateTime dateTime) throws AxelorException {
    Timer timer = find(task);
    return stop(task, timer, dateTime);
  }

  @Override
  public Duration compute(TeamTask task) {
    Duration total = Duration.ZERO;
    for (Timer timer : task.getTimerList()) {
      total = total.plus(compute(timer));
    }
    return total;
  }
}
