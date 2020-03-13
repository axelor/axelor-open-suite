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
package com.axelor.apps.helpdesk.service;

import com.axelor.apps.base.db.Timer;
import com.axelor.apps.base.db.TimerHistory;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.exception.AxelorException;
import java.time.Duration;
import java.time.LocalDateTime;

public interface TimerTicketService {

  Timer find(Ticket ticket) throws AxelorException;

  TimerHistory start(Ticket task, LocalDateTime dateTime) throws AxelorException;

  TimerHistory stop(Ticket task, LocalDateTime dateTime) throws AxelorException;

  void cancel(Ticket task) throws AxelorException;

  Duration compute(Ticket task);
}
