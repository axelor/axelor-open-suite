/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Geo;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.HostInfo;
import net.fortuna.ical4j.util.InetAddressHostInfo;
import net.fortuna.ical4j.util.SimpleHostInfo;
import net.fortuna.ical4j.util.UidGenerator;
import net.fortuna.ical4j.util.Uris;

import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.ICalendar;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.ICalendarUserRepository;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Provides calendars utilities.
 *
 */
public class ICalendarService {

	static final String PRODUCT_ID = "-//Axelor//ADK Calendar 1.0//EN";
	static final String X_WR_CALNAME = "X-WR-CALNAME";

	protected static UidGenerator generator;
	
	@Inject
	protected ICalendarUserRepository iCalendarUserRepository;

	/**
	 * Generate next {@link Uid} to be used with calendar event.
	 *
	 * @return an {@link Uid} instance
	 * @throws SocketException
	 *             if unable to determine host name
	 */
	public static Uid nextUid() throws SocketException {
		if (generator == null) {
			HostInfo info = new SimpleHostInfo("localhost");
			try {
				info = new InetAddressHostInfo(InetAddress.getLocalHost());
			} catch (Exception e) {
			}
			generator = new UidGenerator(info, "" + new SecureRandom().nextInt(Integer.MAX_VALUE));
		}
		return generator.generateUid();
	}

	public static Calendar newCalendar() {
		final Calendar cal = new Calendar();
		cal.getProperties().add(new ProdId(PRODUCT_ID));
		cal.getProperties().add(Version.VERSION_2_0);
		cal.getProperties().add(CalScale.GREGORIAN);
		return cal;
	}

	/**
	 * Load the calendar events from the given source.
	 *
	 * @param calendar
	 *            the target {@link ICalendar}
	 * @param text
	 *            the raw calendar text
	 * @throws ParserException
	 */
	@Transactional
	public void load(ICalendar calendar, String text) throws ParserException {
		Preconditions.checkNotNull(calendar, "calendar can't be null");
		Preconditions.checkNotNull(text, "calendar source can't be null");
		final StringReader reader = new StringReader(text);
		try {
			load(calendar, reader);
		} catch (IOException e) {
		}
	}

	/**
	 * Load the calendar events from the given source file.
	 *
	 * @param calendar
	 *            the target {@link ICalendar}
	 * @param file
	 *            the input file
	 * @throws IOException
	 * @throws ParserException
	 */
	@Transactional
	public void load(ICalendar calendar, File file) throws IOException, ParserException {
		Preconditions.checkNotNull(calendar, "calendar can't be null");
		Preconditions.checkNotNull(file, "input file can't be null");
		Preconditions.checkArgument(file.exists(), "no such file: " + file);

		final Reader reader = new FileReader(file);
		try {
			load(calendar, reader);
		} finally {
			reader.close();
		}
	}

	/**
	 * Load the calendar events from the given reader.
	 *
	 * @param calendar
	 *            the target {@link ICalendar}
	 * @param reader
	 *            the input source reader
	 * @throws IOException
	 * @throws ParserException
	 */
	@Transactional
	public void load(ICalendar calendar, Reader reader) throws IOException, ParserException {
		Preconditions.checkNotNull(calendar, "calendar can't be null");
		Preconditions.checkNotNull(reader, "reader can't be null");

		final CalendarBuilder builder = new CalendarBuilder();
		final Calendar cal = builder.build(reader);

		if (calendar.getName() == null && cal.getProperty(X_WR_CALNAME) != null) {
			calendar.setName(cal.getProperty(X_WR_CALNAME).getValue());
		}

		for (Object item : cal.getComponents(Component.VEVENT)) {
			ICalendarEvent event = findOrCreateEvent((VEvent) item);
			calendar.addEvent(event);
		}
	}

	protected String getValue(Component component, String name) {
		if (component.getProperty(name) != null) {
			return component.getProperty(name).getValue();
		}
		return null;
	}
	
	@Transactional
	protected ICalendarEvent findOrCreateEvent(VEvent vEvent) {

		String uid = vEvent.getUid().getValue();
		DtStart dtStart = vEvent.getStartDate();
		DtEnd dtEnd = vEvent.getEndDate();

		ICalendarEventRepository repo = Beans.get(ICalendarEventRepository.class);
		ICalendarEvent event = repo.findByUid(uid);
		if (event == null) {
			event = new ICalendarEvent();
			event.setUid(uid);
		}

		event.setStartDateTime(new LocalDateTime(dtStart.getDate()));
		event.setEndDateTime(new LocalDateTime(dtEnd.getDate()));
		event.setAllDay(!(dtStart.getDate() instanceof DateTime));

		event.setSubject(getValue(vEvent, Property.SUMMARY));
		event.setDescription(getValue(vEvent, Property.DESCRIPTION));
		event.setLocation(getValue(vEvent, Property.LOCATION));
		event.setGeo(getValue(vEvent, Property.GEO));
		event.setUrl(getValue(vEvent, Property.URL));

		ICalendarUser organizer = findOrCreateUser(vEvent.getOrganizer());
		if (organizer != null) {
			event.setOrganizer(organizer);
			iCalendarUserRepository.save(organizer);
		}

		for (Object item : vEvent.getProperties(Property.ATTENDEE)) {
			ICalendarUser attendee = findOrCreateUser((Property) item);
			if (attendee != null) {
				event.addAttendee(attendee);
				iCalendarUserRepository.save(attendee);
			}
		}

		return event;
	}

	protected ICalendarUser findOrCreateUser(Property source) {
		URI addr = null;
		if (source instanceof Organizer) {
			addr = ((Organizer) source).getCalAddress();
		}
		if (source instanceof Attendee) {
			addr = ((Attendee) source).getCalAddress();
		}
		if (addr == null) {
			return null;
		}

		String email = mailto(addr.toString(), true);
		ICalendarUserRepository repo = Beans.get(ICalendarUserRepository.class);
		ICalendarUser user = repo.findByEmail(email);
		if (user == null) {
			user = new ICalendarUser();
			user.setEmail(email);
		}
		if (source.getParameter(Parameter.CN) != null) {
			user.setName(source.getParameter(Parameter.CN).getValue());
		}

		return user;
	}

	public <T extends Property> T updateUser(T target, ICalendarUser user) {

		if (user == null || user.getEmail() == null) {
			return null;
		}

		String email = mailto(user.getEmail(), false);
		String name = user.getName();

		if (target instanceof Organizer) {
			((Organizer) target).setCalAddress(createUri(email));
		}
		if (target instanceof Attendee) {
			((Attendee) target).setCalAddress(createUri(email));
		}
		if (name != null) {
			target.getParameters().add(new Cn(name));
		}

		return target;
	}

	protected String mailto(final String mail, boolean strip) {
		if (mail == null) {
			return null;
		}
		String res = mail.trim();
		if (strip) {
			if (res.toLowerCase().startsWith("mailto:")) {
				res = res.substring(7);
			}
		} else if (!res.toLowerCase().startsWith("mailto:")){
			res = "mailto:" + res;
		}
		return res;
	}

	protected Date toDate(LocalDateTime dt, boolean allDay) {
		if (dt == null) return null;
		if (allDay) return new Date(dt.toDate());
		return new DateTime(dt.toDate());
	}

	protected URI createUri(String uri) {
		try {
			return Uris.create(uri);
		} catch (URISyntaxException e) {
			throw Throwables.propagate(e);
		}
	}

	protected VEvent createVEvent(ICalendarEvent event) throws SocketException, ParseException {
		boolean allDay = event.getAllDay() == Boolean.TRUE;

		Date start = toDate(event.getStartDateTime(), allDay);
		Date end = toDate(event.getEndDateTime(), allDay);

		VEvent vevent = new VEvent();

		PropertyList items = vevent.getProperties();

		items.add(new DtStart(start));
		if (end != null) {
			items.add(new DtEnd(end));
		}
		if (event.getSubject() != null) {
			items.add(new Summary(event.getSubject()));
		}
		if (event.getDescription() != null) {
			items.add(new Description(event.getDescription()));
		}
		if (event.getStatus() != null) {
			items.add(new Status(event.getStatus()));
		}
		if (event.getLocation() != null) {
			items.add(new Location(event.getLocation()));
		}
		if (event.getGeo() != null) {
			items.add(new Geo(event.getGeo()));
		}
		if (event.getUid() == null) {
			items.add(nextUid());
		} else {
			items.add(new Uid(event.getUid()));
		}
		if (event.getUrl() != null) {
			items.add(createUri(event.getUrl()));
		}
		if (event.getUpdatedOn() != null) {
			DateTime date = new DateTime(event.getUpdatedOn().toDate());
			date.setUtc(true);
			LastModified lastModified = new LastModified(date);
			items.add(lastModified);
		}
		else{
			DateTime date = new DateTime(event.getCreatedOn().toDate());
			date.setUtc(true);
			LastModified lastModified = new LastModified(date);
			items.add(lastModified);
		}

		Organizer organizer = updateUser(new Organizer(), event.getOrganizer());
		if (organizer != null) {
			items.add(organizer);
		}

		if (event.getAttendees() != null) {
			for (ICalendarUser user : event.getAttendees()) {
				Attendee attendee = updateUser(new Attendee(), user);
				if (attendee != null) {
					items.add(attendee);
				}
			}
		}

		return vevent;
	}

	/**
	 * Export the calendar to the given file.
	 *
	 * @param calendar
	 *            the source {@link ICalendar}
	 * @param file
	 *            the target file
	 * @throws IOException
	 * @throws ValidationException
	 * @throws ParseException 
	 */
	public void export(ICalendar calendar, File file) throws IOException, ValidationException, ParseException {
		Preconditions.checkNotNull(calendar, "calendar can't be null");
		Preconditions.checkNotNull(file, "input file can't be null");

		final Writer writer = new FileWriter(file);
		try {
			export(calendar, writer);
		} finally {
			writer.close();
		}
	}

	/**
	 * Export the calendar to the given output writer.
	 *
	 * @param calendar
	 *            the source {@link ICalendar}
	 * @param writer
	 *            the output writer
	 * @throws IOException
	 * @throws ValidationException
	 * @throws ParseException 
	 */
	public void export(ICalendar calendar, Writer writer) throws IOException, ValidationException, ParseException {
		Preconditions.checkNotNull(calendar, "calendar can't be null");
		Preconditions.checkNotNull(writer, "writer can't be null");
		Preconditions.checkNotNull(calendar.getEvents(), "can't export empty calendar");

		Calendar cal = newCalendar();
		cal.getProperties().add(new XProperty(X_WR_CALNAME, calendar.getName()));

		for (ICalendarEvent item : calendar.getEvents()) {
			VEvent event = createVEvent(item);
			cal.getComponents().add(event);
		}

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.output(cal, writer);
	}

	/**
	 * Synchronize the calendar with the given CalDAV collection.
	 *
	 * @param calendar
	 *            the {@link ICalendar} to sync
	 * @param collection
	 *            the remote CalDAV collection
	 * @return the same update {@link ICalendar}
	 * @throws ICalendarException
	 *             if there is any error during sync
	 */
	public ICalendar sync(ICalendar calendar, CalDavCalendarCollection collection)
			throws ICalendarException {
		try {
		return doSync(calendar, collection);
		} catch (Exception e) {
			throw new ICalendarException(e);
		}
	}
	

	protected ICalendar doSync(ICalendar calendar, CalDavCalendarCollection collection)
			throws IOException, URISyntaxException, ParseException, ObjectStoreException, ConstraintViolationException {

		final String[] names = {
			Property.UID,
			Property.URL,
			Property.SUMMARY,
			Property.DESCRIPTION,
			Property.DTSTART,
			Property.DTEND,
			Property.ORGANIZER,
			Property.ATTENDEE
		};

		final boolean keepRemote = calendar.getKeepRemote() == Boolean.TRUE;

		final Map<String, VEvent> remoteEvents = new HashMap<>();
		final List<VEvent> localEvents = new ArrayList<>();
		final Set<String> synced = new HashSet<>();

		for (VEvent item : ICalendarStore.getEvents(collection)) {
			remoteEvents.put(item.getUid().getValue(), item);
		}

		for (ICalendarEvent item : calendar.getEvents()) {
			VEvent source = createVEvent(item);
			VEvent target = remoteEvents.get(source.getUid().getValue());
			if (target == null && Strings.isNullOrEmpty(item.getUid())) {
				target = source;
			}
			
			if(target != null){
				if (keepRemote) {
					VEvent tmp = target;
					target = source;
					source = tmp;
				}
				else{
					if(source.getLastModified() != null && target.getLastModified() != null){
						LocalDateTime lastModifiedSource = new LocalDateTime(source.getLastModified().getDateTime());
						LocalDateTime lastModifiedTarget = new LocalDateTime(target.getLastModified().getDateTime());
						if(lastModifiedSource.isBefore(lastModifiedTarget)){
							VEvent tmp = target;
							target = source;
							source = tmp;
						}
					}
					else if(target.getLastModified() != null){
						VEvent tmp = target;
						target = source;
						source = tmp;
					}
				}
				localEvents.add(target);
				synced.add(target.getUid().getValue());

				if (source == target) {
					continue;
				}

				for (String name : names) {
					Property s = source.getProperty(name);
					Property t = target.getProperty(name);
					if (s == null && t == null) {
						continue;
					}
					if (t == null) {
						t = s;
					}
					if (s == null) {
						target.getProperties().remove(t);
					} else {
						t.setValue(s.getValue());
					}
				}
			}
		}

		for (String uid : remoteEvents.keySet()) {
			if (!synced.contains(uid)) {
				localEvents.add(remoteEvents.get(uid));
			}
		}

		// update local events
		final List<ICalendarEvent> iEvents = new ArrayList<>();
		for (VEvent item : localEvents) {
			ICalendarEvent iEvent = findOrCreateEvent(item);
			iEvents.add(iEvent);
		}
		calendar.getEvents().clear();
		for (ICalendarEvent event : iEvents) {
			calendar.addEvent(event);
		}

		// update remote events
		for (VEvent item : localEvents) {
			if (!synced.contains(item.getUid().getValue())) {
				continue;
			}
			Calendar cal = newCalendar();
			cal.getComponents().add(item);
			collection.addCalendar(cal);
		}

		return calendar;
	}
}
