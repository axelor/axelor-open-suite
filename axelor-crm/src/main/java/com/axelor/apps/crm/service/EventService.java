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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Event;
import com.axelor.auth.db.User;
import com.axelor.message.db.EmailAddress;
import com.axelor.meta.CallMethod;
import java.time.LocalDateTime;

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

  public EmailAddress getEmailAddress(Event event);

  public void fillEventDates(Event event) throws AxelorException;

  public void planEvent(Event event);

  public void realizeEvent(Event event);

  public void cancelEvent(Event event);
}
