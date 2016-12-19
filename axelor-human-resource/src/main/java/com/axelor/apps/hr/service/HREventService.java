package com.axelor.apps.hr.service;

import java.io.IOException;
import java.text.ParseException;

import javax.mail.MessagingException;

import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.service.EventService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

import net.fortuna.ical4j.model.ValidationException;

public class HREventService extends EventService {
	
	
	@Override
	public void sendMail(Event event, String email) throws AxelorException, MessagingException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ValidationException, ParseException, ICalendarException{
	
		User user = Beans.get(UserRepository.class).all().filter("self.partner.emailAddress.address = ?1", email).fetchOne();
		if(user.getActiveCompany().getHrConfig().getSendMail() == true) {
			super.sendMail(event, email);
		}
	}
}
