package com.axelor.apps.base.service.timer;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
import com.axelor.apps.base.db.repo.TimerHistoryRepository;
import com.axelor.apps.base.db.repo.TimerRepository;
import com.google.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class AbstractTimerService implements TimerService {

  protected TimerRepository timerRepository;
  protected TimerHistoryRepository timerHistoryRepository;

  @Inject
  public AbstractTimerService(
      TimerRepository timerRepository, TimerHistoryRepository timerHistoryRepository) {
    this.timerRepository = timerRepository;
    this.timerHistoryRepository = timerHistoryRepository;
  }

  @Override
  public LocalDateTime findStartDate(Timer timer) {
    TimerHistory first =
        timerHistoryRepository
            .all()
            .filter("self.timer = :timer")
            .bind("timer", timer)
            .order("startDate")
            .fetchOne();
    return (first != null ? first.getStartDate() : null);
  }

  @Override
  public LocalDateTime findEndDate(Timer timer) {
    TimerHistory last =
        timerHistoryRepository
            .all()
            .filter("self.timer = :timer")
            .bind("timer", timer)
            .order("-endDate")
            .fetchOne();
    return (last != null ? last.getEndDate() : null);
  }

  @Override
  public Duration findDuration(Timer timer) {
    LocalDateTime start = findStartDate(timer);
    if (start == null) {
      return null;
    }
    LocalDateTime end = findEndDate(timer);
    if (end == null) {
      return null;
    }
    return Duration.between(start, end);
  }
}
