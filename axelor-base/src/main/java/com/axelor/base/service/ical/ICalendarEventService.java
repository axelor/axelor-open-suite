/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.base.service.ical;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.db.RecurrenceConfiguration;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.message.db.EmailAddress;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;

public interface ICalendarEventService {

  List<ICalendarUser> addEmailGuest(EmailAddress email, ICalendarEvent event)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, MessagingException, IOException, ICalendarException, ParseException;

  void addRecurrentEventsByDays(
      ICalendarEvent icevent,
      int periodicity,
      int endType,
      int repetitionsNumber,
      LocalDate endDate);

  void addRecurrentEventsByWeeks(
      ICalendarEvent icevent,
      int periodicity,
      int endType,
      int repetitionsNumber,
      LocalDate endDate,
      Map<Integer, Boolean> daysCheckedMap);

  void generateRecurrentEvents(ICalendarEvent icevent, RecurrenceConfiguration conf)
      throws AxelorException;

  void addRecurrentEventsByMonths(
      ICalendarEvent icevent,
      int periodicity,
      int endType,
      int repetitionsNumber,
      LocalDate endDate,
      int monthRepeatType);

  void addRecurrentEventsByYears(
      ICalendarEvent icevent,
      int periodicity,
      int endType,
      int repetitionsNumber,
      LocalDate endDate);

  String computeRecurrenceName(RecurrenceConfiguration recurrConf);

  void applyChangesToAll(ICalendarEvent event);

  void deleteAll(Long eventId);

  void changeAll(Long eventId, RecurrenceConfiguration config) throws AxelorException;

  void deleteNext(Long eventId);

  void deleteThis(Long eventId);

  void deleteAllByParentId(Long eventId);
}
