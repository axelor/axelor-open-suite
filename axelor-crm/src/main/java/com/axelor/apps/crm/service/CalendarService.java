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

import com.axelor.apps.base.db.CalendarManagement;
import com.axelor.apps.base.db.ICalendar;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.ICalendarRepository;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.auth.db.User;
import com.axelor.team.db.Team;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CalendarService extends ICalendarService {

  @Inject private ICalendarEventRepository icalEventRepo;

  @Inject private EventRepository eventRepo;

  @Inject private ICalendarRepository icalRepo;

  public List<Long> showSharedEvents(User user) {
    Team team = user.getActiveTeam();
    Set<User> followedUsers = user.getFollowersCalUserSet();
    List<Long> eventIdlist = new ArrayList<Long>();
    followedUsers.add(user);

    for (User userIt : followedUsers) {
      for (CalendarManagement calendarManagement : userIt.getCalendarManagementList()) {
        if ((user.equals(calendarManagement.getUser()))
            || (team != null && team.equals(calendarManagement.getTeam()))) {
          if (calendarManagement.getAllCalendars()) {
            List<ICalendarEvent> eventList =
                icalEventRepo
                    .all()
                    .filter(
                        "self.user.id = ?1 or self.calendar.user.id = ?1"
                            + " or self.attendees.user.id = ?1 or self.organizer.user.id = ?1",
                        userIt.getId())
                    .fetch();
            eventIdlist.addAll(Lists.transform(eventList, it -> it.getId()));
          } else {
            if (calendarManagement.getErpCalendars()) {
              List<Event> eventList =
                  eventRepo
                      .all()
                      .filter(
                          "self.calendar = null and (self.user.id = ?1 "
                              + " or self.attendees.user.id = ?1 or self.organizer.user.id = ?1)",
                          userIt.getId())
                      .fetch();
              eventIdlist.addAll(Lists.transform(eventList, it -> it.getId()));
            }
            if (calendarManagement.getIcalCalendars()) {
              List<ICalendarEvent> eventList =
                  icalEventRepo
                      .all()
                      .filter(
                          "self.calendar in (?2) and (self.user.id = ?1 or self.calendar.user.id = ?1"
                              + " or self.attendees.user.id = ?1 or  self.organizer.user.id = ?1)",
                          userIt.getId(),
                          calendarManagement.getCalendarSet())
                      .fetch();

              eventIdlist.addAll(Lists.transform(eventList, it -> it.getId()));
            }
          }
        }
      }
    }

    return eventIdlist;
  }

  public List<Long> showSharedCalendars(User user) {
    Team team = user.getActiveTeam();
    Set<User> followedUsers = user.getFollowersCalUserSet();
    List<Long> calendarIdlist = new ArrayList<Long>();

    for (User userIt : followedUsers) {
      for (CalendarManagement calendarManagement : userIt.getCalendarManagementList()) {
        if ((user.equals(calendarManagement.getUser()))
            || (team != null && team.equals(calendarManagement.getTeam()))) {
          List<ICalendar> icalList =
              icalRepo.all().filter("self.user.id = ?1", userIt.getId()).fetch();
          calendarIdlist.addAll(Lists.transform(icalList, it -> it.getId()));
        }
      }
    }
    List<ICalendar> icalList = icalRepo.all().filter("self.user.id = ?1", user.getId()).fetch();
    calendarIdlist.addAll(Lists.transform(icalList, it -> it.getId()));
    return calendarIdlist;
  }
}
