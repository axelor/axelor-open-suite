/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.RecurrenceConfiguration;
import com.axelor.auth.db.User;
import com.axelor.message.db.EmailAddress;
import com.axelor.meta.CallMethod;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public interface EventService {

  void saveEvent(Event event);

  Event createEvent(
      LocalDateTime fromDateTime,
      LocalDateTime toDateTime,
      User user,
      String description,
      int type,
      String subject);

  @CallMethod
  String getInvoicingAddressFullName(Partner partner);

  void addRecurrentEventsByDays(
      Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate);

  void addRecurrentEventsByWeeks(
      Event event,
      int periodicity,
      int endType,
      int repetitionsNumber,
      LocalDate endDate,
      Map<Integer, Boolean> daysCheckedMap);

  void addRecurrentEventsByMonths(
      Event event,
      int periodicity,
      int endType,
      int repetitionsNumber,
      LocalDate endDate,
      int monthRepeatType);

  void addRecurrentEventsByYears(
      Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate);

  void applyChangesToAll(Event event);

  String computeRecurrenceName(RecurrenceConfiguration recurrConf) throws AxelorException;

  void generateRecurrentEvents(Event event, RecurrenceConfiguration conf) throws AxelorException;

  public EmailAddress getEmailAddress(Event event);

  public void fillEventDates(Event event) throws AxelorException;

  public void planEvent(Event event);

  public void realizeEvent(Event event);

  public void cancelEvent(Event event);
}
