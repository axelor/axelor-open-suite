package com.axelor.apps.crm.db.repo;

import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.ICalendarUserRepository;
import com.axelor.apps.crm.db.Calendar;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.service.CalendarService;
import com.axelor.apps.crm.service.EventService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class EventManagementRepository extends EventRepository {
	
	@Inject
	protected CalendarService calendarService;

	@Override
	public Event copy(Event entity, boolean deep) {
		int eventType=entity.getTypeSelect();
		switch(eventType){
			case 1: //call
			case 2: //metting
				break;
			case 3: //task s
				entity.setTaskStatusSelect(1);
				break;
			case 5: //tickets
				entity.setTicketStatusSelect(1);
				entity.setProgressSelect(0);
				break;

		}
		return super.copy(entity, deep);
	}
	
	@Override
	public Event save(Event entity){
		if(entity.getTypeSelect() == EventRepository.TYPE_MEETING){
			super.save(entity);
			Beans.get(EventService.class).manageFollowers(entity);
		}
		User creator = entity.getCreatedBy();
		if(creator == null){
			creator = AuthUtils.getUser();
		}
		if(entity.getOrganizer() == null && creator != null){
			if(creator.getPartner() != null && creator.getPartner().getEmailAddress() != null){
				String email = creator.getPartner().getEmailAddress().getAddress();
				if(!Strings.isNullOrEmpty(email)){
					ICalendarUser organizer = Beans.get(ICalendarUserRepository.class).all().filter("self.email = ?1 AND self.user.id = ?2",email, creator.getId()).fetchOne();
					if(organizer == null){
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
		if(entity.getVisibilitySelect() == ICalendarEventRepository.VISIBILITY_PRIVATE){
			entity.setSubjectTeam(I18n.get("Available"));
			if(entity.getDisponibilitySelect() == ICalendarEventRepository.DISPONIBILITY_BUSY){
				entity.setSubjectTeam(I18n.get("Busy"));
			}
		}
		
		return super.save(entity);
	}
	
	
	@Override
	public void remove(Event entity) {
		try{
			calendarService.removeEventFromIcal(entity);
		}
		catch(Exception e){
			TraceBackService.trace(e);
		}
		Calendar calendar = entity.getCalendarCrm();
		super.remove(entity);
		if(calendar != null){
			try{
				calendarService.sync(calendar);
			}
			catch(Exception e){
				TraceBackService.trace(e);
			}
		}
		
	}

}
