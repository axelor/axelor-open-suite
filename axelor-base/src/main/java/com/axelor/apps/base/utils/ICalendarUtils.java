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
package com.axelor.apps.base.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendar;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.base.ical.ICalendarStore;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.service.CipherService;
import com.google.common.base.Strings;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fortuna.ical4j.connector.FailedOperationException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.PathResolver;
import net.fortuna.ical4j.model.component.VEvent;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;

public class ICalendarUtils {

  public static void removeEventFromIcal(ICalendarEvent event)
      throws MalformedURLException, ICalendarException {
    if (event.getCalendar() != null && !Strings.isNullOrEmpty(event.getUid())) {
      ICalendar calendar = event.getCalendar();
      PathResolver resolver = ICalendarUtils.getPathResolver(calendar.getTypeSelect());
      Protocol protocol = ICalendarUtils.getProtocol(calendar.getIsSslConnection());
      URL url = new URL(protocol.getScheme(), calendar.getUrl(), calendar.getPort(), "");
      ICalendarStore store = new ICalendarStore(url, resolver);
      try {
        if (store.connect(
            calendar.getLogin(), getCalendarDecryptPassword(calendar.getPassword()))) {
          List<CalDavCalendarCollection> colList = store.getCollections();
          if (!colList.isEmpty()) {
            CalDavCalendarCollection collection = colList.get(0);
            final Map<String, VEvent> remoteEvents = new HashMap<>();

            for (VEvent item : ICalendarStore.getEvents(collection)) {
              remoteEvents.put(item.getUid().getValue(), item);
            }

            VEvent target = remoteEvents.get(event.getUid());
            if (target != null) removeCalendar(collection, target.getUid().getValue());
          }
        } else {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BaseExceptionMessage.CALENDAR_NOT_VALID));
        }
      } catch (Exception e) {
        throw new ICalendarException(e);
      } finally {
        store.disconnect();
      }
    }
  }

  public static net.fortuna.ical4j.model.Calendar removeCalendar(
      CalDavCalendarCollection collection, String uid)
      throws FailedOperationException, ObjectStoreException {
    net.fortuna.ical4j.model.Calendar calendar = collection.getCalendar(uid);

    DeleteMethod deleteMethod = new DeleteMethod(collection.getPath() + uid + ".ics");
    try {
      collection.getStore().getClient().execute(deleteMethod);
    } catch (IOException e) {
      throw new ObjectStoreException(e);
    }
    if (!deleteMethod.succeeded()) {
      throw new FailedOperationException(deleteMethod.getStatusLine().toString());
    }

    return calendar;
  }

  public static PathResolver getPathResolver(int typeSelect) {
    switch (typeSelect) {
      case ICalendarRepository.ICAL_SERVER:
        return PathResolver.ICAL_SERVER;

      case ICalendarRepository.CALENDAR_SERVER:
        return PathResolver.CALENDAR_SERVER;

      case ICalendarRepository.GCAL:
        return PathResolver.GCAL;

      case ICalendarRepository.ZIMBRA:
        return PathResolver.ZIMBRA;

      case ICalendarRepository.KMS:
        return PathResolver.KMS;

      case ICalendarRepository.CGP:
        return PathResolver.CGP;

      case ICalendarRepository.CHANDLER:
        return PathResolver.CHANDLER;

      default:
        return null;
    }
  }

  public static Protocol getProtocol(boolean isSslConnection) {
    if (isSslConnection) {
      return Protocol.getProtocol("https");
    } else {
      return Protocol.getProtocol("http");
    }
  }

  public static String getCalendarDecryptPassword(String password) {
    return Beans.get(CipherService.class).decrypt(password);
  }
}
