package com.axelor.apps.base.service.timer;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import java.time.Duration;
import java.time.LocalDateTime;

public interface TimerService {

  LocalDateTime findStartDate(Timer timer);

  LocalDateTime findEndDate(Timer timer);

  Duration compute(Timer timer);

  Timer find(Model model);

  TimerHistory start(Model model, Timer timer, LocalDateTime dateTime) throws AxelorException;

  default TimerHistory resume(Model model, Timer timer, LocalDateTime dateTime)
      throws AxelorException {
    return start(model, timer, dateTime);
  }

  TimerHistory stop(Model model, Timer timer, LocalDateTime dateTime) throws AxelorException;

  default TimerHistory pause(Model model, Timer timer, LocalDateTime dateTime)
      throws AxelorException {
    return stop(model, timer, dateTime);
  }

  void cancel(Model model, Timer timer, LocalDateTime dateTime);
}
