package com.axelor.base.service.ical;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.mail.MessagingException;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.exception.AxelorException;

import net.fortuna.ical4j.model.ValidationException;

public interface ICalendarEventService {

	List<ICalendarUser> addEmailGuest(EmailAddress email, ICalendarEvent event)	throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxelorException, MessagingException, IOException, ICalendarException, ValidationException, ParseException;

}
