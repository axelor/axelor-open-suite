package com.axelor.apps.base.service.timer;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
import com.axelor.apps.base.db.TimerState;
import com.axelor.apps.base.db.repo.TimerHistoryRepository;
import com.axelor.apps.base.db.repo.TimerRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractTimerService implements TimerService {

  protected TimerRepository timerRepository;
  protected TimerHistoryRepository timerHistoryRepository;
  protected UserService userService;

  @Inject
  public AbstractTimerService(
      TimerRepository timerRepository,
      TimerHistoryRepository timerHistoryRepository,
      UserService userService) {
    this.timerRepository = timerRepository;
    this.timerHistoryRepository = timerHistoryRepository;
    this.userService = userService;
  }

  @Override
  public LocalDateTime findStartDate(Timer timer) {
    TimerHistory first = timerHistoryRepository.findByTimer(timer).order("startDate").fetchOne();
    return (first != null ? first.getStartDate() : null);
  }

  @Override
  public LocalDateTime findEndDate(Timer timer) {
    TimerHistory last = timerHistoryRepository.findByTimer(timer).order("-endDate").fetchOne();
    return (last != null ? last.getEndDate() : null);
  }

  @Override
  public Duration compute(Timer timer) {
    LocalDateTime start = findStartDate(timer);
    if (start == null) {
      return Duration.ZERO;
    }
    LocalDateTime end = findEndDate(timer);
    if (end == null) {
      TimerHistory lastWithEndDate =
          timerHistoryRepository
              .all()
              .filter("self.timer = :timer AND self.endDate IS NOT NULL")
              .bind("timer", timer)
              .order("-endDate")
              .fetchOne();
      if (lastWithEndDate == null) {
        return Duration.ZERO;
      }
      end = lastWithEndDate.getEndDate();
    }
    return Duration.between(start, end);
  }

  @Override
  @Transactional
  public void cancel(Timer timer) {
    List<TimerHistory> histories = timerHistoryRepository.findByTimer(timer).fetch();
    histories.forEach(timerHistoryRepository::remove);
    timer.setState(TimerState.STOPPED);
  }

  @Transactional
  protected Timer tryStartOrCreate(Timer timer) throws AxelorException {
    if (timer == null) {
      timer = new Timer();
      timer.setAssignedTo(userService.getUser());
    } else if (timer.getState().equals(TimerState.STARTED)) {
      throw new AxelorException(
          TraceBackRepository.TYPE_FUNCTIONNAL, I18n.get(IExceptionMessage.TIMER_IS_NOT_STOPPED));
    }
    timer.setState(TimerState.STARTED);
    return timerRepository.save(timer);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class, RuntimeException.class})
  public TimerHistory stop(Model model, Timer timer, LocalDateTime dateTime)
      throws AxelorException {
    Preconditions.checkNotNull(timer, I18n.get(IExceptionMessage.TIMER_IS_NOT_STARTED));

    TimerHistory last = timerHistoryRepository.findByTimer(timer).order("-startDate").fetchOne();
    if (last == null) {
      throw new AxelorException(
          TraceBackRepository.TYPE_FUNCTIONNAL, I18n.get(IExceptionMessage.TIMER_IS_NOT_STARTED));
    }
    last.setEndDate(dateTime);
    timer.setState(TimerState.STOPPED);

    return last;
  }
}
