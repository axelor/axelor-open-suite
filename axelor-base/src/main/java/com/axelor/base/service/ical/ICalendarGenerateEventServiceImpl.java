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

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.RecurrenceConfiguration;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.google.inject.Inject;

public class ICalendarGenerateEventServiceImpl implements ICalendarGenerateEventService {

  @Inject ICalendarEventServiceImpl iCalendarEventServiceImpl;

  @Inject ICalendarEventRepository eventRepo;

  protected ICalendarEvent event;

  protected RecurrenceConfiguration conf;

  @Override
  public ICalendarEvent call() throws Exception {
    iCalendarEventServiceImpl.deleteAllByParentId(event.getId());
    event = eventRepo.find(event.getId());
    iCalendarEventServiceImpl.generateRecurrentEvents(event, conf);
    return null;
  }

  @Override
  public void setICalendarEvent(ICalendarEvent event, RecurrenceConfiguration conf) {
    this.event = event;
    this.conf = conf;
  }
}
