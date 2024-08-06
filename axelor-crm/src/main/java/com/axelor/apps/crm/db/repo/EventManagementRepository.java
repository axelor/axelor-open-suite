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
package com.axelor.apps.crm.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.ICalendarUserRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.service.CalendarService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;

public class EventManagementRepository extends EventRepository {

  @Inject protected ICalendarService calendarService;

  @Override
  public Event copy(Event entity, boolean deep) {
    Event event = super.copy(entity, deep);
    event.setStatusSelect(EventRepository.STATUS_PLANNED);
    return event;
  }

  @Override
  public Event save(Event entity) {

    User creator = entity.getCreatedBy();
    if (creator == null) {
      creator = AuthUtils.getUser();
    }
    if (entity.getOrganizer() == null && creator != null) {
      if (creator.getPartner() != null && creator.getPartner().getEmailAddress() != null) {
        String email = creator.getPartner().getEmailAddress().getAddress();
        if (!Strings.isNullOrEmpty(email)) {
          ICalendarUser organizer =
              Beans.get(ICalendarUserRepository.class)
                  .all()
                  .filter("self.email = ?1 AND self.user.id = ?2", email, creator.getId())
                  .fetchOne();
          if (organizer == null) {
            organizer = new ICalendarUser();
            organizer.setEmail(email);
            organizer.setName(creator.getFullName());
            organizer.setUser(creator);
          }
          entity.setOrganizer(organizer);
        }
      }
    }

    entity.setSubjectTeam(entity.getSubject());
    if (entity.getVisibilitySelect() == ICalendarEventRepository.VISIBILITY_PRIVATE) {
      entity.setSubjectTeam(I18n.get("Available"));
      if (entity.getDisponibilitySelect() == ICalendarEventRepository.DISPONIBILITY_BUSY) {
        entity.setSubjectTeam(I18n.get("Busy"));
      }
    }

    return super.save(entity);
  }

  @Override
  public void remove(Event entity) {
    remove(entity, true);
  }

  public void remove(Event entity, boolean removeRemote) {

    try {

      if (entity.getCalendar() == null && Strings.isNullOrEmpty(entity.getUid())) {
        //     Not a synchronized event
        super.remove(entity);
        return;
      }

      User user = AuthUtils.getUser();
      List<Long> calendarIdlist = Beans.get(CalendarService.class).showSharedCalendars(user);
      if (calendarIdlist.isEmpty() || !calendarIdlist.contains(entity.getCalendar().getId())) {
        throw new AxelorException(
            entity,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get("You don't have the rights to delete this event"));
      }

      calendarService.removeEventFromIcal(entity);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    entity.setArchived(true);
  }
}
