/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.web;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;

import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.ValidationException;

import com.axelor.apps.crm.service.CalendarService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class CalendarController {

	@Inject
	private CalendarService calendarService;
	
	public void exportCalendar(ActionRequest request, ActionResponse response) throws IOException, ParserException, ValidationException, ObjectStoreException, ObjectNotFoundException {
		
		calendarService.exportCalendar();
	}
	
	public void importCalendar(ActionRequest request, ActionResponse response) throws IOException, ParserException {
		
		calendarService.importCalendar();
	}
	
	public void synchronizeCalendars(ActionRequest request, ActionResponse response) throws MalformedURLException, SocketException, ObjectStoreException, ObjectNotFoundException, ConstraintViolationException {
		
		calendarService.synchronizeCalendars(null);
	}
}
