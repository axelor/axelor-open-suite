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
package com.axelor.apps.base.ical;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.CalDavCalendarStore;
import net.fortuna.ical4j.connector.dav.PathResolver;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.util.CompatibilityHints;
import org.apache.jackrabbit.webdav.DavException;

/**
 * This class delegates the {@link CalDavCalendarStore} and provides most common methods to deal
 * with CalDAV store.
 */
public class ICalendarStore {

  private CalDavCalendarStore deligateStore;

  static {
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_NOTES_COMPATIBILITY, true);
  }

  public ICalendarStore(URL url, PathResolver pathResolver) {
    this.deligateStore = new CalDavCalendarStore(ICalendarService.PRODUCT_ID, url, pathResolver);
  }

  public boolean connect(String username, String password) throws ObjectStoreException {
    if (deligateStore.isConnected()) {
      return true;
    }
    return deligateStore.connect(username, password.toCharArray());
  }

  public boolean connect() {
    if (deligateStore.isConnected()) {
      return true;
    }
    try {
      return deligateStore.connect();
    } catch (ObjectStoreException e) {
    }
    return false;
  }

  public void disconnect() {
    if (deligateStore.isConnected()) {
      deligateStore.disconnect();
    }
  }

  public CalDavCalendarCollection getCollection(String id)
      throws ObjectStoreException, ObjectNotFoundException {
    return deligateStore.getCollection(id);
  }

  public List<CalDavCalendarCollection> getCollections() throws ObjectStoreException {
    try {
      return deligateStore.getCollections();
    } catch (ObjectNotFoundException e) {
      e.printStackTrace();
    }
    return new ArrayList<>();
  }

  public static List<VEvent> getEvents(CalDavCalendarCollection calendar) {
    final List<VEvent> events = new ArrayList<>();
    for (Calendar cal : calendar.getEvents()) {
      for (Object item : cal.getComponents(Component.VEVENT)) {
        VEvent event = (VEvent) item;
        events.add(event);
      }
    }
    return events;
  }

  public static List<VEvent> getModifiedEvents(
      CalDavCalendarCollection calendar, Instant instant, Set<String> remoteUids) {
    final List<VEvent> events = new ArrayList<>();

    for (Calendar cal : calendar.getEvents()) {
      cal.toString();
      for (Object item : ((List<CalendarComponent>) cal.getComponents(Component.VEVENT))) {
        VEvent event = (VEvent) item;
        if (instant == null || event.getLastModified().getDate().toInstant().isAfter(instant)) {
          events.add(event);
        }
        remoteUids.add(event.getUid().getValue());
      }
    }
    return events;
  }

  public static List<VEvent> getModifiedEventsInRange(
      CalDavCalendarCollection calendar,
      Instant instant,
      Set<String> remoteUids,
      LocalDateTime startDate,
      LocalDateTime endDate)
      throws IOException, DavException, ParserConfigurationException, ParserException,
          ParseException {
    final List<VEvent> events = new ArrayList<>();

    DateTime start = new DateTime(Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant()));
    DateTime end = new DateTime(Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant()));
    start = new DateTime(start.toString() + "Z");
    end = new DateTime(end.toString() + "Z");

    for (Calendar cal : calendar.getEventsForTimePeriod(start, end)) {
      cal.toString();
      for (Object item : ((List<CalendarComponent>) cal.getComponents(Component.VEVENT))) {
        VEvent event = (VEvent) item;
        if (event.getLastModified().getDate().toInstant().isAfter(instant)) {
          events.add(event);
        }
        remoteUids.add(event.getUid().getValue());
      }
    }
    return events;
  }

  public CalDavCalendarStore getDelegateStore() {
    return deligateStore;
  }
}
