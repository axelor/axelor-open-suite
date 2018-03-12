package com.axelor.apps.base.web;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import javax.mail.MessagingException;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.base.service.ical.ICalendarEventService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

import net.fortuna.ical4j.model.ValidationException;

public class ICalendarEventController {

	@Inject
	ICalendarEventService iCalendarEventService;

	@SuppressWarnings("unchecked")
	public void addEmailGuest(ActionRequest request, ActionResponse response) throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException{
		ICalendarEvent event = request.getContext().asType(ICalendarEvent.class);
		try{
			Map<String, Object> guestEmail = (Map<String, Object>) request.getContext().get("guestEmail");
			if(guestEmail != null){
				EmailAddress emailAddress = Beans.get(EmailAddressRepository.class).find(new Long((guestEmail.get("id").toString())));
				if(emailAddress != null){
					response.setValue("attendees", iCalendarEventService.addEmailGuest(emailAddress, event));
				}
			}
		} catch(Exception e) {
			TraceBackService.trace(response, e);
		}
	}
}
