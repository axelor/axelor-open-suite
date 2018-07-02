package com.axelor.apps.base.service.timer;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
import com.axelor.db.Model;
import java.time.Duration;
import java.time.LocalDateTime;

public interface TimerService {
  LocalDateTime findStartDate(Timer timer);

  LocalDateTime findEndDate(Timer timer);

  Duration findDuration(Timer timer);

  TimerHistory start(Model model, Timer timer, LocalDateTime dateTime);

  TimerHistory stop(Model model, Timer timer, LocalDateTime dateTime);
}
