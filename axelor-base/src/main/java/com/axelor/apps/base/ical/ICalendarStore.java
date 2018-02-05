/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.ical;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.CalDavCalendarStore;
import net.fortuna.ical4j.connector.dav.PathResolver;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.util.CompatibilityHints;

/**
 * This class delegates the {@link CalDavCalendarStore} and provides most common
 * methods to deal with CalDAV store.
 *
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

	public boolean connect(String username, String password) {
		if (deligateStore.isConnected()) {
			return true;
		}
		try {
			return deligateStore.connect(username, password.toCharArray());
		} catch (ObjectStoreException e) {
			e.printStackTrace();
		}
		return false;
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

	public CalDavCalendarStore getDelegateStore() {
		return deligateStore;
	}
}
