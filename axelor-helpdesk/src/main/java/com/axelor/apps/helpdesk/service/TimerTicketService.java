package com.axelor.apps.helpdesk.service;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.exception.AxelorException;
import java.time.Duration;
import java.time.LocalDateTime;

public interface TimerTicketService {

  Timer find(Ticket ticket);

  TimerHistory start(Ticket task, LocalDateTime dateTime) throws AxelorException;

  TimerHistory stop(Ticket task, LocalDateTime dateTime) throws AxelorException;

  void cancel(Ticket task) throws AxelorException;

  Duration compute(Ticket task);
}
