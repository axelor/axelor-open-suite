/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.crm.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.connector.dav.CalDavCalendarCollection;
import net.fortuna.ical4j.connector.dav.CalDavCalendarStore;
import net.fortuna.ical4j.connector.dav.PathResolver;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.util.UidGenerator;

import org.apache.commons.httpclient.protocol.Protocol;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.crm.db.Calendar;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.EventAttendee;
import com.axelor.apps.crm.db.ICalendar;
import com.google.inject.persist.Transactional;

public class CalendarService {

	private static final Logger LOG = LoggerFactory.getLogger(CalendarService.class);
	
	
	public void exportCalendar() throws IOException, ParserException, ValidationException, ObjectStoreException, ObjectNotFoundException  {
		
		this.createCalendarFile(this.createCalendar());
		
	}
	
	
	
	
	private static class myICal extends PathResolver {

        @Override
        public String getPrincipalPath(String username) {
            return "/DAVTest/";
        }

        @Override
        public String getUserPath(String username) {
        	 return "/DAVTest/";
        }
    }
	
	
	public static class GenericPathResolver extends PathResolver {
		 
         private String principalPath;
         private String userPath;
         
         public String principalPath() {
             return principalPath;
         }
         
         public void setPrincipalPath(String principalPath) {
             this.principalPath = principalPath;
         }
         
         @Override
         public String getPrincipalPath(String username) {
           return principalPath + "/" + username + "/";
         }
 
         public String userPath() {
             return userPath;
         }
         
         public void setUserPath(String userPath) {
             this.userPath = userPath;
         }
         
         @Override
         public String getUserPath(String username) {
             return userPath + "/" + username;
         }
     }
	
	
	public PathResolver getPathResolver(int typeSelect)  {
		switch (typeSelect) {
		case ICalendar.ICAL_SERVER :
			return PathResolver.ICAL_SERVER;

		case ICalendar.CALENDAR_SERVER :
			return PathResolver.CALENDAR_SERVER;

		case ICalendar.GCAL :
			return PathResolver.GCAL;

		case ICalendar.ZIMBRA :
			return PathResolver.ZIMBRA;

		case ICalendar.KMS :
			return PathResolver.KMS;

		case ICalendar.CGP :
			return PathResolver.CGP;
					
		case ICalendar.CHANDLER :
			return PathResolver.CHANDLER;
			
		default:
			return null;
		}
	}
	
	
	public void synchronizeCalendars(UserInfo userInfo) throws MalformedURLException, ObjectStoreException, ObjectNotFoundException, SocketException, ConstraintViolationException  {
	
		for(Calendar internalCalendar : this.getInternalCalendarList())  {
			
			
			List<VEvent> vEventList = this.getExternalCalendar(
					this.getPathResolver(internalCalendar.getTypeSelect()), 
					internalCalendar.getUrl(),
					this.getProtocol(internalCalendar.getIsSslConnection()),
					internalCalendar.getPort(),
					internalCalendar.getLogin(), 
					internalCalendar.getPassword(),
					internalCalendar);
			
			this.getEvent(vEventList, internalCalendar);
			
			this.removeEvent(vEventList, internalCalendar);
			
		}
		
	}
	
	
	public Protocol getProtocol(boolean isSslConnection)  {
		
		if(isSslConnection)  {
			return Protocol.getProtocol("https");
		}
		else  {
			return Protocol.getProtocol("http");
		}
		
	}
	
	
	public List<Calendar> getInternalCalendarList()  {
		
//		List<Calendar> internalCalendarList = Calendar.all().filter("self", UserInfo userInfo) // TODO Récupérer la liste des calendriers du tiers
		
		List<Calendar> internalCalendarList = Calendar.all().fetch();
		
		return internalCalendarList; 
	}
	
		
	
	public List<VEvent> getExternalCalendar(PathResolver pathResolver, String url, Protocol protocol, int port, String login, String password, Calendar internalCalendar) throws MalformedURLException, ObjectStoreException, ObjectNotFoundException, SocketException, ConstraintViolationException  {
		
		String PRODID = "-//Ben Fortuna//iCal4j Connector 1.0//EN";
	
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true); 
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_NOTES_COMPATIBILITY, true);
		
		URL url2 = new URL(protocol.getScheme(), url, port, "");
	  	CalDavCalendarStore store = new CalDavCalendarStore(PRODID, url2, pathResolver);
	  	
	  	if(login != null && password != null)  {
	  		store.connect(login, password.toCharArray());
	  	}
	  	else  {
	  		store.connect();
	  	}
	  	

	  	
	  	List<CalDavCalendarCollection> collections = store.getCollections();
	
		CalDavCalendarCollection collection = (CalDavCalendarCollection) collections.get(0); 

		System.out.println( "collectionID= " + collection.getId() ); 
		
//		CalDavCalendarCollection calDavCalendarCollection = store.getCollection("/calendar/dav/g.dubaux%40axelor.com/events/");
		
//		calDavCalendarCollection = store.get;
   
//		System.out.println( "calDavCalendarCollection.collectionID= " + calDavCalendarCollection.getId() ); 
		
//		  Calendar calendar = collection.getCalendar("g.dubaux@axelor.com");
		
		
		List<Event> eventList = Event.all().filter("self.typeSelect != 6 and self.calendarEventUid IS NULL and self.calendar = ?1",internalCalendar).fetch();
		
	  
		List<VEvent> vEventList = new ArrayList<VEvent>();
		
		CalDavCalendarCollection calDavCalendarCollection2 = null;
		
		for(CalDavCalendarCollection calDavCalendarCollection : collections)  {
		  
//			  Calendar calendar = calDavCalendarCollection.getCalendar("g.dubaux@axelor.com");
		  
			System.out.println( "collectionID2= " + calDavCalendarCollection.getId() ); 
			net.fortuna.ical4j.model.Calendar[] calendars = calDavCalendarCollection.getEvents();

			for(net.fortuna.ical4j.model.Calendar calendar : calendars)  {
				System.out.print("CALENDAR - "+calendar.getProductId());
				if (calendar != null) { 
				  
					if(calDavCalendarCollection2 == null)  {
						calDavCalendarCollection2 = calDavCalendarCollection;
					}
					
				  	vEventList.addAll(calendar.getComponents(Component.VEVENT));
				  	for(Event event : eventList)  {
				  		calendar.getComponents(Component.VEVENT).add(this.createVEvent(event));
					}
				  	calDavCalendarCollection.addCalendar(calendar);
			  	}
			}
			
			
//			store.merge(calDavCalendarCollection.getId(), calDavCalendarCollection);
		}
		
		for (VEvent vEvent : vEventList) {
		  	System.out.print(vEvent.getProperty(Property.SUMMARY));
		  	System.out.print(vEvent.getProperty(Property.DTSTART));
		  	System.out.print(" - ");
		  	System.out.println(vEvent.getProperty(Property.DTEND));
	  	}
		
//		Collection collection = new Collection();
//		for(Calendar calendar : Calendar.all().fetch())  {
//			List<Event> eventList = Event.all().filter("self.typeSelect != 6 and self.calendarEventUid IS NULL and self.calendar = ?1",calendar).fetch();
//			for(Event event : eventList)  {
//				this.createVEvent(event);
//			}
//		}
		
//		calDavCalendarCollection2.getCalendar(uid)
//		store.getCollections().add(calDavCalendarCollection2);
//		store.merge(calDavCalendarCollection2.getId(), calDavCalendarCollection2);
		
//		store.
		
		store.disconnect();
		
		return vEventList;
	}
	
	
	
	
	
	
	
	
	public net.fortuna.ical4j.model.Calendar createCalendar() throws SocketException  {
		
		net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
		calendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);

		// Add events, etc..
		// Add the event and print
//		calendar.getComponents().add(this.createEvent());
		
		calendar.getComponents().addAll(this.createEvents(Event.all().fetch()));
				
		return calendar;
	}
	
	
	
	public List<VEvent> createEvents(List<Event> eventList) throws SocketException  {
		
		List<VEvent> vEventList = new ArrayList<VEvent>();
		
		if(eventList != null)  {
			for(Event event : eventList)  {
				vEventList.add(this.createVEvent(event));
			}
		}
		
		return vEventList;
		
	}
	
	
	
	public VEvent createVEvent(Event event) throws SocketException  {
		LOG.debug("Create VEvent from "+event);
		
		// Create a TimeZone
		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		TimeZone timezone = registry.getTimeZone("Europe/Paris");
		VTimeZone tz = timezone.getVTimeZone();

		java.util.Calendar startDate = new GregorianCalendar();
		startDate.setTimeZone(timezone);
		if(event.getStartDateTime() != null)  {
			startDate.setTime(event.getStartDateTime().toDate());
		}

		java.util.Calendar endDate = new GregorianCalendar();
		endDate.setTimeZone(timezone);
		if(event.getEndDateTime() != null)  {
			endDate.setTime(event.getEndDateTime().toDate());
		}
		
		// Create the event
		String eventName = event.getSubject();
		DateTime start = new DateTime(startDate.getTime());
		DateTime end = new DateTime(endDate.getTime());
		VEvent vEvent = new VEvent(start, end, eventName);

		// add timezone info..
		vEvent.getProperties().add(tz.getTimeZoneId());

		// generate unique identifier..
		if(event.getCalendarEventUid()!= null && !event.getCalendarEventUid().isEmpty())  {
			vEvent.getProperties().add(new Uid(event.getCalendarEventUid()));
		}
		else  {
			UidGenerator ug = new UidGenerator("uidGen");
			Uid uid = ug.generateUid();
			vEvent.getProperties().add(uid);

			this.updateEvent(event, uid.getValue());
		}
		
		
		// add attendees..
		vEvent.getProperties().addAll(this.createAttendees(event));
		
		return vEvent;
		
	}
	
	@Transactional
	public void updateEvent(Event event, String uid)  {
		
		event.setCalendarEventUid(uid);
		event.save();
		
	}
	
	
	public List<Attendee> createAttendees(Event event)  {
		
		List<Attendee> attendeeList = new ArrayList<Attendee>();
		
		if(event.getContactEventAttendeeList() != null)  {
			for(EventAttendee eventAttendee : event.getContactEventAttendeeList())  {
				attendeeList.add(this.createAttendee(eventAttendee));
			}
		}
		if(event.getLeadEventAttendeeList() != null)  {
			for(EventAttendee eventAttendee : event.getLeadEventAttendeeList())  {
				attendeeList.add(this.createAttendee(eventAttendee));
			}
		}
		
		return attendeeList;
	}
	
	
	public Attendee createAttendee(EventAttendee eventAttendee)  {
		
		Attendee attendee = null;
		
		if(eventAttendee.getContactPartner() != null)  {
			attendee = new Attendee(URI.create("mailto:"+eventAttendee.getContactPartner().getEmailAddress().getAddress()));
			attendee.getParameters().add(new Cn(eventAttendee.getContactPartner().getName()+eventAttendee.getContactPartner().getFirstName()));
		}
		else if(eventAttendee.getLead() != null)  {
			attendee = new Attendee(URI.create("mailto:"+eventAttendee.getLead().getEmailAddress().getAddress()));
			attendee.getParameters().add(new Cn(eventAttendee.getLead().getName()+eventAttendee.getLead().getFirstName()));
		}
		
		attendee.getParameters().add(this.getRole(eventAttendee));
		
		return attendee;
		
	}
	
	
	public Role getRole(EventAttendee eventAttendee)  {
		
		switch(eventAttendee.getStatusSelect())  {
			case 1:
				return Role.OPT_PARTICIPANT;
			case 2:
				return Role.OPT_PARTICIPANT;
			case 3:
				return Role.REQ_PARTICIPANT;
			default:
				return null;
		}		
	}
	
	
	
	public void createCalendarFile(net.fortuna.ical4j.model.Calendar calendar) throws IOException, ValidationException  {
		
		FileOutputStream fout = new FileOutputStream("mycalendar.ics");

		CalendarOutputter outputter = new CalendarOutputter();
		outputter.output(calendar, fout);
		
	}
	
	
	
	public void importCalendar() throws IOException, ParserException  {
		
		List<VEvent> vEventList = this.getVEvent(this.getCalendar());
		
		this.getEvent(vEventList, null);
		
		this.removeEvent(vEventList, null);
		
		this.getInfo(this.getCalendar());
	}
	
	
	
	public net.fortuna.ical4j.model.Calendar getCalendar() throws IOException, ParserException  {
		
		FileInputStream fin = new FileInputStream("mycalendar.ics");

		CalendarBuilder builder = new CalendarBuilder();

		net.fortuna.ical4j.model.Calendar calendar = builder.build(fin);
		
		return calendar;
		
	}
	
	
	
	public void getEvent(List<VEvent> vEventList, Calendar internalCalendar)  {
		
		for(VEvent vEvent : vEventList)  {
			
			Event event = Event.all().filter("self.calendarEventUid = ?1", vEvent.getUid().getValue()).fetchOne(); 
			if(event != null)  {
				
				this.updateEvent(event, vEvent);
				
			}
			else  {
				this.createEvent(vEvent, internalCalendar);
			}
		}
		
	}
	
	public void removeEvent(List<VEvent> vEventList, Calendar internalCalendar)  {
		
		List<String> uidList = new ArrayList<String>();
		
		for(VEvent vEvent : vEventList)  {
			uidList.add(vEvent.getUid().getValue());
		}
		
		List<Event> eventList = null;
		
		if(uidList != null && uidList.size() > 0)  {
			eventList = Event.all().filter("self.typeSelect = ?1 AND self.calendar = ?2 AND self.calendarEventUid not in ?3", 6, internalCalendar, uidList).fetch();
		}
		else  {
			eventList = Event.all().filter("self.typeSelect = ?1 AND self.calendar = ?2", 6, internalCalendar).fetch();
		}
		
		for(Event event : eventList)  {
			
			this.removeEvent(event);
			
		}
	}
	
	
	@Transactional
	public void removeEvent(Event event)  {  
		
		event.remove();
		
	}
	
	
	public Event createEvent(VEvent vEvent, Calendar internalCalendar)  {
		
		Event event = new Event();
		event.setTypeSelect(6);
		event.setCalendarEventUid(vEvent.getUid().getValue());
		
		event.setCalendar(internalCalendar);
		
		this.updateEvent(event, vEvent);
		
		return event;
	}
	
	
	@Transactional
	public Event updateEvent(Event event, VEvent vEvent)  {

		event.setSubject(vEvent.getSummary().getValue());
		if(vEvent.getDescription()!=null)  {
			event.setDescription(vEvent.getDescription().getValue());
		}
		event.setStartDateTime(new LocalDateTime(vEvent.getStartDate().getDate()));
		event.setEndDateTime(new LocalDateTime(vEvent.getEndDate().getDate()));
		event.save();
		
		return event;
	}
	
	
	
	public List<VEvent> getVEvent(net.fortuna.ical4j.model.Calendar calendar)  {
		
		return calendar.getComponents(Component.VEVENT);
		
	}
	
	
	public void getInfo(net.fortuna.ical4j.model.Calendar calendar)  {
		
		for (Iterator i = calendar.getComponents().iterator(); i.hasNext();) {
		    Component component = (Component) i.next();
		    LOG.debug("Component [" + component.getName() + "]");
			
		    for (Iterator j = component.getProperties().iterator(); j.hasNext();) {
		        Property property = (Property) j.next();
		        LOG.debug("Property [" + property.getName() + ", " + property.getValue() + "]");
		        
		    }
		}
	}
	
	
//	public void createVCard()  {
//		
//		List<Property> props = new ArrayList<Property>();
//		props.add(new Source(URI.create("ldap://ldap.example.com/cn=Babs%20Jensen,%20o=Babsco,%20c=US")));
//		props.add(new Name("Babs Jensen's Contact Information"));
//		props.add(Kind.INDIVIDUAL);
//		// add a custom property..
//		props.add(new Property("test") {
//		    @Override
//		    public String getValue() {
//		        return null;
//		    }
//
//		    @Override
//		    public void validate() throws ValidationException {
//		    }
//		});
//
//		VCard vcard = new VCard(props);
//		return vcard;
//	}
	
	

	public VEvent createEventTest() throws SocketException  {
		
		// Create a TimeZone
		TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
		TimeZone timezone = registry.getTimeZone("America/Mexico_City");
		VTimeZone tz = timezone.getVTimeZone();

		 // Start Date is on: April 1, 2008, 9:00 am
		java.util.Calendar startDate = new GregorianCalendar();
		startDate.setTimeZone(timezone);
		startDate.set(java.util.Calendar.MONTH, java.util.Calendar.APRIL);
		startDate.set(java.util.Calendar.DAY_OF_MONTH, 1);
		startDate.set(java.util.Calendar.YEAR, 2008);
		startDate.set(java.util.Calendar.HOUR_OF_DAY, 9);
		startDate.set(java.util.Calendar.MINUTE, 0);
		startDate.set(java.util.Calendar.SECOND, 0);

		 // End Date is on: April 1, 2008, 13:00
		java.util.Calendar endDate = new GregorianCalendar();
		endDate.setTimeZone(timezone);
		endDate.set(java.util.Calendar.MONTH, java.util.Calendar.APRIL);
		endDate.set(java.util.Calendar.DAY_OF_MONTH, 1);
		endDate.set(java.util.Calendar.YEAR, 2008);
		endDate.set(java.util.Calendar.HOUR_OF_DAY, 13);
		endDate.set(java.util.Calendar.MINUTE, 0);	
		endDate.set(java.util.Calendar.SECOND, 0);

		// Create the event
		String eventName = "Progress Meeting";
		DateTime start = new DateTime(startDate.getTime());
		DateTime end = new DateTime(endDate.getTime());
		VEvent meeting = new VEvent(start, end, eventName);

		// add timezone info..
		meeting.getProperties().add(tz.getTimeZoneId());

		// generate unique identifier..
		UidGenerator ug = new UidGenerator("uidGen");
		Uid uid = ug.generateUid();
		meeting.getProperties().add(uid);

		// add attendees..
		Attendee dev1 = new Attendee(URI.create("mailto:dev1@mycompany.com"));
		dev1.getParameters().add(Role.REQ_PARTICIPANT);
		dev1.getParameters().add(new Cn("Developer 1"));
		meeting.getProperties().add(dev1);

		Attendee dev2 = new Attendee(URI.create("mailto:dev2@mycompany.com"));
		dev2.getParameters().add(Role.OPT_PARTICIPANT);
		dev2.getParameters().add(new Cn("Developer 2"));
		meeting.getProperties().add(dev2);
		
		return meeting;
		
	}
}
