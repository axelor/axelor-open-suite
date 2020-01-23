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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class ICalendarEventManagementRepository extends ICalendarEventRepository {

  @Inject private ICalendarService calendarService;

  @Override
  public ICalendarEvent save(ICalendarEvent entity) {

    User creator = entity.getCreatedBy();
    if (creator == null) {
      creator = AuthUtils.getUser();
    }
    if (entity.getOrganizer() == null && creator != null) {
      if (creator.getPartner() != null && creator.getPartner().getEmailAddress() != null) {
        String email = creator.getPartner().getEmailAddress().getAddress();
        if (email != null) {
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
  public void remove(ICalendarEvent entity) {
    remove(entity, true);
  }

  public void remove(ICalendarEvent entity, boolean removeRemote) {
    try {
      if (removeRemote) {
        calendarService.removeEventFromIcal(entity);
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    super.remove(entity);
  }
}
