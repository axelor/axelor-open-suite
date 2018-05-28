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
package com.axelor.apps.crm.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.ical.ICalendarService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.RecurrenceConfiguration;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.RecurrenceConfigurationRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailAddress;
import com.axelor.mail.db.MailFollower;
import com.axelor.mail.db.repo.MailAddressRepository;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EventServiceImpl implements EventService {
	
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("dd/MM");

	private PartnerService partnerService;
	
	private EventRepository eventRepo;
	

	@Inject
	public EventServiceImpl(EventAttendeeService eventAttendeeService, PartnerService partnerService, EventRepository eventRepository,
							MailFollowerRepository mailFollowerRepo, ICalendarService iCalendarService, MessageService messageService,
							TemplateMessageService templateMessageService) {
		this.partnerService = partnerService;
		this.eventRepo = eventRepository;
	}

	@Override
	@Transactional
	public void saveEvent(Event event){
		eventRepo.save(event);
	}


	@Override
	public Event createEvent(LocalDateTime fromDateTime, LocalDateTime toDateTime, User user, String description, int type, String subject){
		Event event = new Event();
		event.setSubject(subject);
		event.setStartDateTime(fromDateTime);
		event.setEndDateTime(toDateTime);
		event.setUser(user);
		event.setTypeSelect(type);
		if(!Strings.isNullOrEmpty(description)){
			event.setDescription(description);
		}

        if (fromDateTime != null && toDateTime != null) {
            long duration = Duration.between(fromDateTime, toDateTime).getSeconds();
            event.setDuration(duration);
        }

		return event;
	}

	@Override
	public String getInvoicingAddressFullName(Partner partner) {

		Address address = partnerService.getInvoicingAddress(partner);
		if(address != null){
			return address.getFullName();
		}
		
		return null;
	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void manageFollowers(Event event){
		MailFollowerRepository mailFollowerRepo = Beans.get(MailFollowerRepository.class);
		List<MailFollower> followers = mailFollowerRepo.findAll(event);
		List<ICalendarUser> attendeesSet = event.getAttendees();

		if(followers != null)
			followers.forEach(x -> mailFollowerRepo.remove(x));
		mailFollowerRepo.follow(event, event.getUser());

		if(attendeesSet != null){
			for (ICalendarUser user : attendeesSet) {
				if(user.getUser() != null){
					mailFollowerRepo.follow(event, user.getUser());
				}
				else{
					MailAddress mailAddress = Beans.get(MailAddressRepository.class).findOrCreate(user.getEmail(), user.getName());
					mailFollowerRepo.follow(event, mailAddress);
				}
			}
		}
	}


	@Override
	@Transactional
	public void addRecurrentEventsByDays(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate){
		Event lastEvent = event;
		if(endType == 1){
			int repeated = 0;
			while(repeated != repetitionsNumber){
				Event copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusDays(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusDays(periodicity));
				eventRepo.save(copy);
				repeated++;
				lastEvent = copy;
			}
		}
		else{
			while(!lastEvent.getStartDateTime().plusDays(periodicity).isAfter(endDate.atStartOfDay())){
				Event copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusDays(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusDays(periodicity));
				eventRepo.save(copy);
				lastEvent = copy;
			}
		}
	}

	@Override
	@Transactional
	public void addRecurrentEventsByWeeks(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate, Map<Integer, Boolean> daysCheckedMap){
		Event lastEvent = event;
		List<Integer> list = new ArrayList<Integer>();
		for (int day : daysCheckedMap.keySet()) {
			list.add(day);
		}
		Collections.sort(list);
		if(endType == 1){
			int repeated = 0;
			Event copy = eventRepo.copy(lastEvent, false);
			copy.setParentEvent(lastEvent);
			int dayOfWeek = copy.getStartDateTime().getDayOfWeek().getValue();
			LocalDateTime nextDateTime = LocalDateTime.now();
			if(dayOfWeek < list.get(0)){
				nextDateTime = copy.getStartDateTime().plusDays(list.get(0) - dayOfWeek);
			}
			else if(dayOfWeek > list.get(0)){
				nextDateTime = copy.getStartDateTime().plusDays((7-dayOfWeek)+list.get(0));
			}
			Duration dur = Duration.between(copy.getStartDateTime(), copy.getEndDateTime());
			
			for (Integer integer : list) {
				nextDateTime.plusDays(integer - nextDateTime.getDayOfWeek().getValue());
				copy.setStartDateTime(nextDateTime);
				copy.setEndDateTime(nextDateTime.plus(dur));
				eventRepo.save(copy);
				lastEvent = copy;
				repeated++;
			}
			
			while(repeated < repetitionsNumber){
				copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusWeeks(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusWeeks(periodicity));
				
				dayOfWeek = copy.getStartDateTime().getDayOfWeek().getValue();
				nextDateTime = LocalDateTime.now();
				if(dayOfWeek < list.get(0)){
					nextDateTime =	copy.getStartDateTime().plusDays(list.get(0) - dayOfWeek);
				}
				else if(dayOfWeek > list.get(0)){
					nextDateTime = copy.getStartDateTime().plusDays((7-dayOfWeek)+list.get(0));
				}
				dur = Duration.between(copy.getStartDateTime(), copy.getEndDateTime());
				
				for (Integer integer : list) {
					nextDateTime.plusDays(integer - nextDateTime.getDayOfWeek().getValue());
					copy.setStartDateTime(nextDateTime);
					copy.setEndDateTime(nextDateTime.plus(dur));
					eventRepo.save(copy);
					lastEvent = copy;
					repeated++;
				}
			}
		}
		else{
			
			Event copy = eventRepo.copy(lastEvent, false);
			copy.setParentEvent(lastEvent);
			int dayOfWeek = copy.getStartDateTime().getDayOfWeek().getValue();
			LocalDateTime nextDateTime = LocalDateTime.now();
			if(dayOfWeek < list.get(0)){
				nextDateTime = copy.getStartDateTime().plusDays(list.get(0) - dayOfWeek);
			}
			else if(dayOfWeek > list.get(0)){
				nextDateTime = copy.getStartDateTime().plusDays((7-dayOfWeek)+list.get(0));
			}
			Duration dur = Duration.between(copy.getStartDateTime(), copy.getEndDateTime());
			
			for (Integer integer : list) {
				nextDateTime.plusDays(integer - nextDateTime.getDayOfWeek().getValue());
				copy.setStartDateTime(nextDateTime);
				copy.setEndDateTime(nextDateTime.plus(dur));
				eventRepo.save(copy);
				lastEvent = copy;
			}
			
			while(!copy.getStartDateTime().plusWeeks(periodicity).isAfter(endDate.atStartOfDay())){
				copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusWeeks(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusWeeks(periodicity));
				
				dayOfWeek = copy.getStartDateTime().getDayOfWeek().getValue();
				nextDateTime = LocalDateTime.now();
				if(dayOfWeek < list.get(0)){
					nextDateTime = copy.getStartDateTime().plusDays(list.get(0) - dayOfWeek);
				}
				else if(dayOfWeek > list.get(0)){
					nextDateTime = copy.getStartDateTime().plusDays((7-dayOfWeek)+list.get(0));
				}
				dur = Duration.between(copy.getStartDateTime(), copy.getEndDateTime());
				
				for (Integer integer : list) {
					nextDateTime.plusDays(integer - nextDateTime.getDayOfWeek().getValue());
					copy.setStartDateTime(nextDateTime);
					copy.setEndDateTime(nextDateTime.plus(dur));
					eventRepo.save(copy);
					lastEvent = copy;
				}
			}
		}
	}

	@Override
	@Transactional
	public void addRecurrentEventsByMonths(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate, int monthRepeatType){
		Event lastEvent = event;
		if(monthRepeatType == 1){
			int dayOfMonth = event.getStartDateTime().getDayOfMonth();
			if(endType == 1){
				int repeated = 0;
				while(repeated != repetitionsNumber){
					Event copy = eventRepo.copy(lastEvent, false);
					copy.setParentEvent(lastEvent);
					if(copy.getStartDateTime().plusMonths(periodicity).toLocalDate().lengthOfMonth() >= dayOfMonth){
						copy.setStartDateTime(copy.getStartDateTime().plusMonths(periodicity));
						copy.setEndDateTime(copy.getEndDateTime().plusMonths(periodicity));
						eventRepo.save(copy);
						repeated++;
						lastEvent = copy;
					}
				}
			}
			else{
				while(!lastEvent.getStartDateTime().plusMonths(periodicity).isAfter(endDate.atStartOfDay())){
					Event copy = eventRepo.copy(lastEvent, false);
					copy.setParentEvent(lastEvent);
					if(copy.getStartDateTime().plusMonths(periodicity).toLocalDate().lengthOfMonth() >= dayOfMonth){
						copy.setStartDateTime(copy.getStartDateTime().plusMonths(periodicity));
						copy.setEndDateTime(copy.getEndDateTime().plusMonths(periodicity));
						eventRepo.save(copy);
						lastEvent = copy;
					}
				}
			}
		}
		
		else{
			int dayOfWeek = event.getStartDateTime().getDayOfWeek().getValue();
			int positionInMonth = 0;
			if(event.getStartDateTime().getDayOfMonth() % 7 == 0){
				positionInMonth = event.getStartDateTime().getDayOfMonth() / 7;
			}
			else{
				positionInMonth = (event.getStartDateTime().getDayOfMonth() / 7) + 1;
			}
			
			if(endType == 1){
				int repeated = 0;
				while(repeated != repetitionsNumber){
					Event copy = eventRepo.copy(lastEvent, false);
					copy.setParentEvent(lastEvent);
					LocalDateTime nextDateTime = copy.getStartDateTime();
					nextDateTime.plusMonths(periodicity);
					int nextDayOfWeek = nextDateTime.getDayOfWeek().getValue();
					if(nextDayOfWeek > dayOfWeek){
						nextDateTime.minusDays(nextDayOfWeek - dayOfWeek);
					}
					else{
						nextDateTime.plusDays(dayOfWeek - nextDayOfWeek);
					}
					int nextPositionInMonth = 0;
					if(event.getStartDateTime().getDayOfMonth() % 7 == 0){
						nextPositionInMonth = event.getStartDateTime().getDayOfMonth() / 7;
					}
					else{
						nextPositionInMonth = (event.getStartDateTime().getDayOfMonth() / 7) + 1;
					}
					if(nextPositionInMonth > positionInMonth){
						nextDateTime.minusWeeks(nextPositionInMonth - positionInMonth);
					}
					else{
						nextDateTime.plusWeeks(positionInMonth - nextPositionInMonth);
					}
					Duration dur = Duration.between(copy.getStartDateTime(), copy.getEndDateTime());
					copy.setStartDateTime(nextDateTime);
					copy.setEndDateTime(nextDateTime.plus(dur));
					eventRepo.save(copy);
					repeated++;
					lastEvent = copy;
				}
			}
			else{
				LocalDateTime nextDateTime = lastEvent.getStartDateTime();
				nextDateTime.plusMonths(periodicity);
				int nextDayOfWeek = nextDateTime.getDayOfWeek().getValue();
				if(nextDayOfWeek > dayOfWeek){
					nextDateTime.minusDays(nextDayOfWeek - dayOfWeek);
				}
				else{
					nextDateTime.plusDays(dayOfWeek - nextDayOfWeek);
				}
				int nextPositionInMonth = 0;
				if(event.getStartDateTime().getDayOfMonth() % 7 == 0){
					nextPositionInMonth = event.getStartDateTime().getDayOfMonth() / 7;
				}
				else{
					nextPositionInMonth = (event.getStartDateTime().getDayOfMonth() / 7) + 1;
				}
				if(nextPositionInMonth > positionInMonth){
					nextDateTime.minusWeeks(nextPositionInMonth - positionInMonth);
				}
				else{
					nextDateTime.plusWeeks(positionInMonth - nextPositionInMonth);
				}
				while(!nextDateTime.isAfter(endDate.atStartOfDay())){
					Event copy = eventRepo.copy(lastEvent, false);
					copy.setParentEvent(lastEvent);
					
					Duration dur = Duration.between(copy.getStartDateTime(), copy.getEndDateTime());
					copy.setStartDateTime(nextDateTime);
					copy.setEndDateTime(nextDateTime.plus(dur));
					eventRepo.save(copy);
					lastEvent = copy;
					
					nextDateTime = lastEvent.getStartDateTime();
					nextDateTime.plusMonths(periodicity);
					nextDayOfWeek = nextDateTime.getDayOfWeek().getValue();
					if(nextDayOfWeek > dayOfWeek){
						nextDateTime.minusDays(nextDayOfWeek - dayOfWeek);
					}
					else{
						nextDateTime.plusDays(dayOfWeek - nextDayOfWeek);
					}
					nextPositionInMonth = 0;
					if(event.getStartDateTime().getDayOfMonth() % 7 == 0){
						nextPositionInMonth = event.getStartDateTime().getDayOfMonth() / 7;
					}
					else{
						nextPositionInMonth = (event.getStartDateTime().getDayOfMonth() / 7) + 1;
					}
					if(nextPositionInMonth > positionInMonth){
						nextDateTime.minusWeeks(nextPositionInMonth - positionInMonth);
					}
					else{
						nextDateTime.plusWeeks(positionInMonth - nextPositionInMonth);
					}
				}
			}
		}
	}

	@Override
	@Transactional
	public void addRecurrentEventsByYears(Event event, int periodicity, int endType, int repetitionsNumber, LocalDate endDate){
		Event lastEvent = event;
		if(endType == 1){
			int repeated = 0;
			while(repeated != repetitionsNumber){
				Event copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusYears(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusYears(periodicity));
				
				eventRepo.save(copy);
				repeated++;
				lastEvent = copy;
			}
		}
		else{
			while(!lastEvent.getStartDateTime().plusYears(periodicity).isAfter(endDate.atStartOfDay())){
				Event copy = eventRepo.copy(lastEvent, false);
				copy.setParentEvent(lastEvent);
				copy.setStartDateTime(copy.getStartDateTime().plusYears(periodicity));
				copy.setEndDateTime(copy.getEndDateTime().plusYears(periodicity));
				eventRepo.save(copy);
				lastEvent = copy;
			}
		}
	}

	@Override
	@Transactional
	public void applyChangesToAll(Event event){
		
		Event child = eventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
		Event parent = event.getParentEvent();
		Event copyEvent = eventRepo.copy(event, false);
		while(child != null){
			child.setSubject(event.getSubject());
			child.setCalendar(event.getCalendar());
			child.setStartDateTime(child.getStartDateTime().withHour(event.getStartDateTime().getHour()));
			child.setStartDateTime(child.getStartDateTime().withMinute(event.getStartDateTime().getMinute()));
			child.setEndDateTime(child.getEndDateTime().withHour(event.getEndDateTime().getHour()));
			child.setEndDateTime(child.getEndDateTime().withMinute(event.getEndDateTime().getMinute()));
			child.setDuration(event.getDuration());
			child.setUser(event.getUser());
			child.setTeam(event.getTeam());
			child.setDisponibilitySelect(event.getDisponibilitySelect());
			child.setVisibilitySelect(event.getVisibilitySelect());
			child.setDescription(event.getDescription());
			child.setPartner(event.getPartner());
			child.setContactPartner(event.getContactPartner());
			child.setLead(event.getLead());
			child.setTypeSelect(event.getTypeSelect());
			child.setLocation(event.getLocation());
			eventRepo.save(child);
			copyEvent = child;
			child = eventRepo.all().filter("self.parentEvent.id = ?1", copyEvent.getId()).fetchOne();
		}
		while(parent != null){
			Event nextParent = parent.getParentEvent();
			parent.setSubject(event.getSubject());
			parent.setCalendar(event.getCalendar());
			parent.setStartDateTime(parent.getStartDateTime().withHour(event.getStartDateTime().getHour()));
			parent.setStartDateTime(parent.getStartDateTime().withMinute(event.getStartDateTime().getMinute()));
			parent.setEndDateTime(parent.getEndDateTime().withHour(event.getEndDateTime().getHour()));
			parent.setEndDateTime(parent.getEndDateTime().withMinute(event.getEndDateTime().getMinute()));
			parent.setDuration(event.getDuration());
			parent.setUser(event.getUser());
			parent.setTeam(event.getTeam());
			parent.setDisponibilitySelect(event.getDisponibilitySelect());
			parent.setVisibilitySelect(event.getVisibilitySelect());
			parent.setDescription(event.getDescription());
			parent.setPartner(event.getPartner());
			parent.setContactPartner(event.getContactPartner());
			parent.setLead(event.getLead());
			parent.setTypeSelect(event.getTypeSelect());
			parent.setLocation(event.getLocation());
			eventRepo.save(parent);
			parent = nextParent;
		}
	}

	@Override
	public String computeRecurrenceName(RecurrenceConfiguration recurrConf){
		String recurrName = "";
		switch (recurrConf.getRecurrenceType()) {
		case RecurrenceConfigurationRepository.TYPE_DAY:
			if(recurrConf.getPeriodicity() == 1){
				recurrName += I18n.get("Every day");
			}
			else{
				recurrName += String.format(I18n.get("Every %d days"), recurrConf.getPeriodicity());
			}
			
			if(recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET){
				recurrName += String.format(", " + I18n.get("%d times"), recurrConf.getRepetitionsNumber());
			}
			else if(recurrConf.getEndDate() != null){
				recurrName += ", " + I18n.get("until the") + " " + recurrConf.getEndDate().format(DATE_FORMAT);
			}
			break;
		
		case RecurrenceConfigurationRepository.TYPE_WEEK:
			if(recurrConf.getPeriodicity() == 1){
				recurrName += I18n.get("Every week") + " ";
			}
			else{
				recurrName += String.format(I18n.get("Every %d weeks") + " ", recurrConf.getPeriodicity());
			}
			if(recurrConf.getMonday() && recurrConf.getTuesday() && recurrConf.getWednesday() && recurrConf.getThursday() && recurrConf.getFriday()
					&& !recurrConf.getSaturday() && !recurrConf.getSunday()){
				recurrName += I18n.get("every week's day");
			}
			else if(recurrConf.getMonday() && recurrConf.getTuesday() && recurrConf.getWednesday() && recurrConf.getThursday() && recurrConf.getFriday() 
					&& recurrConf.getSaturday() && recurrConf.getSunday()){
				recurrName += I18n.get("everyday");
			}
			else{
				recurrName += I18n.get("on") + " ";
				if(recurrConf.getMonday()){
					recurrName += I18n.get("mon,");
				}
				if(recurrConf.getTuesday()){
					recurrName += I18n.get("tues,");
				}
				if(recurrConf.getWednesday()){
					recurrName += I18n.get("wed,");
				}
				if(recurrConf.getThursday()){
					recurrName += I18n.get("thur,");
				}
				if(recurrConf.getFriday()){
					recurrName += I18n.get("fri,");
				}
				if(recurrConf.getSaturday()){
					recurrName += I18n.get("sat,");
				}
				if(recurrConf.getSunday()){
					recurrName += I18n.get("sun,");
				}
			}
			
			if(recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET){
				recurrName += String.format(" " + I18n.get("%d times"), recurrConf.getRepetitionsNumber());
			}
			else if(recurrConf.getEndDate() != null){
				recurrName += " " + I18n.get("until the") + " " + recurrConf.getEndDate().format(DATE_FORMAT);
			}
			break;
		
		case RecurrenceConfigurationRepository.TYPE_MONTH:
			if(recurrConf.getPeriodicity() == 1){
				recurrName += I18n.get("Every month the") + " " + recurrConf.getStartDate().getDayOfMonth();
			}
			else{
				recurrName += String.format(I18n.get("Every %d months the %d"), recurrConf.getPeriodicity(), recurrConf.getStartDate().getDayOfMonth());
			}
			
			if(recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET){
				recurrName += String.format(", " + I18n.get("%d times"), recurrConf.getRepetitionsNumber());
			}
			else if(recurrConf.getEndDate() != null){
				recurrName += ", " + I18n.get("until the") + " " + recurrConf.getEndDate().format(DATE_FORMAT);
			}
			break;
			
		case RecurrenceConfigurationRepository.TYPE_YEAR:
			if(recurrConf.getPeriodicity() == 1){
				recurrName += I18n.get("Every year the") + recurrConf.getStartDate().format(MONTH_FORMAT);
			}
			else{
				recurrName += String.format(I18n.get("Every %d years the %s"), recurrConf.getPeriodicity(), recurrConf.getStartDate().format(MONTH_FORMAT));
			}
			
			if(recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET){
				recurrName += String.format(", " + I18n.get("%d times"), recurrConf.getRepetitionsNumber());
			}
			else if(recurrConf.getEndDate() != null){
				recurrName +=  ", " + I18n.get("until the")+ " " +recurrConf.getEndDate().format(DATE_FORMAT);
			}
			break;

		default:
			break;
		}
		return recurrName;
	}

	@Override
	public void generateRecurrentEvents(Event event, RecurrenceConfiguration conf) throws AxelorException {
		if(conf.getRecurrenceType() == null){
			throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, IExceptionMessage.RECURRENCE_RECURRENCE_TYPE);
		}

		int recurrenceType = new Integer(conf.getRecurrenceType().toString());

		if(conf.getPeriodicity() == null){
			throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.RECURRENCE_PERIODICITY));
		}

		int periodicity = new Integer(conf.getPeriodicity().toString());

		if(periodicity < 1){
			throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.RECURRENCE_PERIODICITY));
		}

		boolean monday = (boolean) conf.getMonday();
		boolean tuesday = (boolean) conf.getTuesday();
		boolean wednesday = (boolean) conf.getWednesday();
		boolean thursday = (boolean) conf.getThursday();
		boolean friday = (boolean) conf.getFriday();
		boolean saturday = (boolean) conf.getSaturday();
		boolean sunday = (boolean) conf.getSunday();
		Map<Integer,Boolean> daysMap = new HashMap<Integer,Boolean>();
		Map<Integer,Boolean> daysCheckedMap = new HashMap<Integer,Boolean>();
		if(recurrenceType == RecurrenceConfigurationRepository.TYPE_WEEK){
			daysMap.put(DayOfWeek.MONDAY.getValue(), monday);
			daysMap.put(DayOfWeek.TUESDAY.getValue(), tuesday);
			daysMap.put(DayOfWeek.WEDNESDAY.getValue(), wednesday);
			daysMap.put(DayOfWeek.THURSDAY.getValue(), thursday);
			daysMap.put(DayOfWeek.FRIDAY.getValue(), friday);
			daysMap.put(DayOfWeek.SATURDAY.getValue(), saturday);
			daysMap.put(DayOfWeek.SUNDAY.getValue(), sunday);

			for (Integer day : daysMap.keySet()) {
				if(daysMap.get(day)){
					daysCheckedMap.put(day, daysMap.get(day));
				}
			}
			if(daysMap.isEmpty()){
				throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.RECURRENCE_DAYS_CHECKED));
			}
		}

		int monthRepeatType = new Integer(conf.getMonthRepeatType().toString());

		int endType = new Integer(conf.getEndType().toString());

		int repetitionsNumber = 0;

		if(endType == RecurrenceConfigurationRepository.END_TYPE_REPET ){
			if(conf.getRepetitionsNumber() == null){
				throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.RECURRENCE_REPETITION_NUMBER));
			}

			repetitionsNumber = new Integer(conf.getRepetitionsNumber().toString());

			if(repetitionsNumber < 1){
				throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.RECURRENCE_REPETITION_NUMBER));
			}
		}
		LocalDate endDate = LocalDate.now();
		if(endType == RecurrenceConfigurationRepository.END_TYPE_DATE){
			if(conf.getEndDate() == null){
				throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.RECURRENCE_END_DATE));
			}

			endDate = LocalDate.parse(conf.getEndDate().toString(), DateTimeFormatter.ISO_DATE);

			if(endDate.isBefore(event.getStartDateTime().toLocalDate()) && endDate.isEqual(event.getStartDateTime().toLocalDate())){
				throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get(IExceptionMessage.RECURRENCE_END_DATE));
			}
		}
		switch (recurrenceType) {
		case RecurrenceConfigurationRepository.TYPE_DAY:
			addRecurrentEventsByDays(event, periodicity, endType, repetitionsNumber, endDate);
			break;

		case RecurrenceConfigurationRepository.TYPE_WEEK:
			addRecurrentEventsByWeeks(event, periodicity, endType, repetitionsNumber, endDate, daysCheckedMap);
			break;

		case RecurrenceConfigurationRepository.TYPE_MONTH:
			addRecurrentEventsByMonths(event, periodicity, endType, repetitionsNumber, endDate, monthRepeatType);
			break;

		case RecurrenceConfigurationRepository.TYPE_YEAR:
			addRecurrentEventsByYears(event, periodicity, endType, repetitionsNumber, endDate);
			break;

		default:
			break;
		}
	}
}