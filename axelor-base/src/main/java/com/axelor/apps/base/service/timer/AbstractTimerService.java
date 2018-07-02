package com.axelor.apps.base.service.timer;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
import com.axelor.apps.base.db.repo.TimerHistoryRepository;
import com.axelor.apps.base.db.repo.TimerRepository;
import com.axelor.db.Model;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
  public void cancel(Model model, Timer timer, LocalDateTime dateTime) {
    List<TimerHistory> histories = timerHistoryRepository.findByTimer(timer).fetch();
    histories.forEach(timerHistoryRepository::remove);
  }
}
