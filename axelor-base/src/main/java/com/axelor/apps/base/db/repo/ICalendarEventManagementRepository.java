package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class ICalendarEventManagementRepository extends ICalendarEventRepository{
	
	@Override
	public ICalendarEvent save(ICalendarEvent entity){

		User creator = entity.getCreatedBy();
		if(creator == null){
			creator = AuthUtils.getUser();
		}
		if(entity.getOrganizer() == null && creator != null){
			if(creator.getPartner() != null && creator.getPartner().getEmailAddress() != null){
				String email = creator.getPartner().getEmailAddress().getAddress();
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
		
		
		entity.setSubjectTeam(entity.getSubject());
		if(entity.getVisibilitySelect() == ICalendarEventRepository.VISIBILITY_PRIVATE){
			entity.setSubjectTeam(I18n.get("Available"));
			if(entity.getDisponibilitySelect() == ICalendarEventRepository.DISPONIBILITY_BUSY){
				entity.setSubjectTeam(I18n.get("Busy"));
			}
		}
		
		return super.save(entity);
	}
}
