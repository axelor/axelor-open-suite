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
package com.axelor.apps.crm.service.batch;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.message.MessageServiceCrmImpl;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;

public class BatchEventReminderMessage extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchEventReminderMessage.class);

	private boolean stop = false;
	
	@Inject
	private EventService eventService;
	
	@Inject
	public BatchEventReminderMessage(MessageServiceCrmImpl messageServiceCrmImpl, MailAccountService mailAccountService) {
		
		super(messageServiceCrmImpl, mailAccountService);
	}
	
	
	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
		
		super.start();
		
	}

	
	@Override
	public void process() {
	
		if(!stop)  {
			
			int i = 0;
			
			Query q = JPA.em().createQuery("select event FROM EventReminder as er WHERE er.isReminded = true and ?1 MEMBER OF (er.batchSet)");
			q.setParameter(1, batch);
					
			@SuppressWarnings("unchecked")
			List<Event> eventList = q.getResultList();
				
			for(Event event : eventList)  {
				try {
					messageServiceCrmImpl.createMessage( event );
				} catch (Exception e) {
					
					TraceBackService.trace(new Exception(String.format(I18n.get("Event")+" %s", 
							eventService.find(event.getId()).getSubject()), e), IException.CRM, batch.getId());
					
					incrementAnomaly();
					
					LOG.error("Bug(Anomalie) généré(e) pour l'évènement {}", eventService.find(event.getId()).getSubject());
					
				} finally {
					
					if (i % 1 == 0) { JPA.clear(); }
		
				}	
			}
		}
		
		
	}
	
	
	
	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = I18n.get(IExceptionMessage.BATCH_EVENT_REMINDER_2);
		comment += String.format("\t* %s "+I18n.get(IExceptionMessage.BATCH_EVENT_REMINDER_MESSAGE_1)+"\n", batch.getDone());
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());
		
		super.stop();
		addComment(comment);
		
	}

}
