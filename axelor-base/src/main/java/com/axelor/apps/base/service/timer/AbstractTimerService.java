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
package com.axelor.apps.base.service.timer;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
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
    TimerHistory first = timerHistoryRepository.findByTimer(timer).order("startDateT").fetchOne();
    return (first != null ? first.getStartDateT() : null);
  }

  @Override
  public LocalDateTime findEndDate(Timer timer) {
    TimerHistory last = timerHistoryRepository.findByTimer(timer).order("-endDateT").fetchOne();
    return (last != null ? last.getEndDateT() : null);
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
              .filter("self.timer = :timer AND self.endDateT IS NOT NULL")
              .bind("timer", timer)
              .order("-endDateT")
              .fetchOne();
      if (lastWithEndDate == null) {
        return Duration.ZERO;
      }
      end = lastWithEndDate.getEndDateT();
    }
    return Duration.between(start, end);
  }

  @Override
  @Transactional
  public void cancel(Timer timer) {
    List<TimerHistory> histories = timerHistoryRepository.findByTimer(timer).fetch();
    histories.forEach(timerHistoryRepository::remove);
    timer.setStatusSelect(TimerRepository.TIMER_STOPPED);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Timer tryStartOrCreate(Timer timer) throws AxelorException {
    if (timer == null) {
      timer = new Timer();
      timer.setAssignedToUser(userService.getUser());
    } else if (timer.getStatusSelect().equals(TimerRepository.TIMER_STARTED)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.TIMER_IS_NOT_STOPPED));
    }
    timer.setStatusSelect(TimerRepository.TIMER_STARTED);
    return timerRepository.save(timer);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public TimerHistory stop(Model model, Timer timer, LocalDateTime dateTime)
      throws AxelorException {
    Preconditions.checkNotNull(timer, I18n.get(IExceptionMessage.TIMER_IS_NOT_STARTED));

    TimerHistory last = timerHistoryRepository.findByTimer(timer).order("-startDateT").fetchOne();
    if (last == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.TIMER_IS_NOT_STARTED));
    }
    last.setEndDateT(dateTime);
    timer.setStatusSelect(TimerRepository.TIMER_STOPPED);

    return last;
  }
}
