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
package com.axelor.apps.base.ical;

import com.axelor.apps.base.db.ICalendar;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ICalendarEventFactory {

  private static final Map<String, Supplier<ICalendarEvent>> map = new HashMap<>();

  static {
    map.put(ICalendarRepository.ICAL_ONLY, ICalendarEvent::new);
  }

  public static ICalendarEvent getNewIcalEvent(ICalendar calendar) {
    Supplier<ICalendarEvent> supplier =
        map.getOrDefault(calendar.getSynchronizationSelect(), ICalendarEvent::new);
    return supplier.get();
  }

  public static void register(String selection, Supplier<ICalendarEvent> eventSupplier) {
    map.put(selection, eventSupplier);
  }
}
