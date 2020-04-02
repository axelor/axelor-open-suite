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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.RecurrenceConfiguration;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
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

  String getInvoicingAddressFullName(Partner partner);

  void manageFollowers(Event event);

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

  String computeRecurrenceName(RecurrenceConfiguration recurrConf);

  void generateRecurrentEvents(Event event, RecurrenceConfiguration conf) throws AxelorException;
}
