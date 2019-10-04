/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ICalendar;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.repo.ICalendarRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.text.ParseException;
import net.fortuna.ical4j.data.ParserException;

@Singleton
public class ICalendarController {

  public void exportCalendar(ActionRequest request, ActionResponse response)
      throws IOException, ParseException {
    ICalendar cal = request.getContext().asType(ICalendar.class);
    Path tempPath = MetaFiles.createTempFile(cal.getName(), ".ics");
    Beans.get(ICalendarService.class).export(cal, tempPath.toFile());
    Beans.get(MetaFiles.class)
        .attach(new FileInputStream(tempPath.toFile()), cal.getName() + ".ics", cal);
    response.setReload(true);
  }

  public void importCalendarFile(ActionRequest request, ActionResponse response)
      throws IOException, ParserException {

    ImportConfiguration imp = request.getContext().asType(ImportConfiguration.class);
    Object object = request.getContext().get("_id");
    ICalendar cal = null;
    if (object != null) {
      Long id = Long.valueOf(object.toString());
      cal = Beans.get(ICalendarRepository.class).find(id);
    }

    if (cal == null) {
      cal = new ICalendar();
    }
    File data = MetaFiles.getPath(imp.getDataMetaFile()).toFile();
    Beans.get(ICalendarService.class).load(cal, data);
    response.setCanClose(true);
    response.setReload(true);
  }

  public void importCalendar(ActionRequest request, ActionResponse response) {
    ICalendar cal = request.getContext().asType(ICalendar.class);
    response.setView(
        ActionView.define(I18n.get(IExceptionMessage.IMPORT_CALENDAR))
            .model("com.axelor.apps.base.db.ImportConfiguration")
            .add("form", "import-icalendar-form")
            .param("popup", "reload")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .context("_id", cal.getId())
            .map());
  }

  public void testConnect(ActionRequest request, ActionResponse response) {
    try {
      ICalendar cal = request.getContext().asType(ICalendar.class);
      Beans.get(ICalendarService.class).testConnect(cal);
      response.setValue("isValid", true);
    } catch (Exception e) {
      TraceBackService.trace(e);
      response.setFlash("Configuration error");
      response.setValue("isValid", false);
    }
  }

  public void synchronizeCalendar(ActionRequest request, ActionResponse response)
      throws MalformedURLException, ICalendarException {
    ICalendar cal = request.getContext().asType(ICalendar.class);
    cal = Beans.get(ICalendarRepository.class).find(cal.getId());
    Beans.get(ICalendarService.class).sync(cal, false, 0);
    response.setReload(true);
  }

  public void validate(ActionRequest request, ActionResponse response) {

    if (request.getContext().get("newPassword") != null)
      response.setValue(
          "password",
          Beans.get(ICalendarService.class)
              .getCalendarEncryptPassword(request.getContext().get("newPassword").toString()));
  }

  public void showMyEvents(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();

    response.setView(
        ActionView.define(I18n.get("My events"))
            .model(ICalendarEvent.class.getName())
            .add("calendar", "calendar-event-all")
            .add("grid", "calendar-event-grid")
            .add("form", "calendar-event-form")
            .domain(
                "self.user.id = :_userId"
                    + " OR self.calendar.user.id = :_userId"
                    + " OR :_userId IN (SELECT attendee.user FROM self.attendees attendee)"
                    + " OR self.organizer.user.id = :_userId"
                    + " OR :_userId IN (SELECT setting.sharedWith FROM self.calendar.sharingSettingList setting WHERE setting.visible = TRUE)")
            .context("_userId", user.getId())
            .map());
  }
}
