/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.RecurrenceConfiguration;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import net.fortuna.ical4j.model.ValidationException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EventService {
    Duration computeDuration(LocalDateTime startDateTime, LocalDateTime endDateTime);

    int getDuration(Duration duration);

    LocalDateTime computeStartDateTime(int duration, LocalDateTime endDateTime);

    LocalDateTime computeEndDateTime(LocalDateTime startDateTime, int duration);

    void saveEvent(Event event);

    void addLeadAttendee(Event event, Lead lead, Partner contactPartner);

    Event createEvent(LocalDateTime fromDateTime, LocalDateTime toDateTime, User user, String description, int type, String subject);

    String getInvoicingAddressFullName(Partner partner);

    void manageFollowers(Event event);

    Event checkModifications(Event event, Event previousEvent) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, MessagingException;

    <T> List<T> deletedGuests (Set<T> previousSet, Set<T> set);

    <T> List<T> addedGuests (Set<T> previousSet, Set<T> set);

    void sendMails(Event event) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, MessagingException;

    void addEmailGuest(EmailAddress email, Event event) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException;

    void sendMail(Event event, String email) throws AxelorException, MessagingException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ValidationException, ParseException, ICalendarException;

    void addRecurrentEventsByDays(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate);

    void addRecurrentEventsByWeeks(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate, Map<Integer, Boolean> daysCheckedMap);

    void addRecurrentEventsByMonths(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate, int monthRepeatType);

    void addRecurrentEventsByYears(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate);

    void applyChangesToAll(Event event);

    String computeRecurrenceName(RecurrenceConfiguration recurrConf);
}
