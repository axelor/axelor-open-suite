/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.apps.message.service.MessageService;
import com.axelor.db.JPA;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchEventReminderMessage extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchEventReminderMessage.class);

	private boolean stop = false;
	
	@Inject
	public BatchEventReminderMessage(MessageService messageService, MailAccountService mailAccountService) {
		
		super(messageService, mailAccountService);
	}
	
	
	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
		
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
					messageService.createMessage(event, mailAccountService.getDefaultMailAccount());
				} catch (Exception e) {
					
					TraceBackService.trace(new Exception(String.format("Event %s", 
							Event.find(event.getId()).getSubject()), e), IException.CRM, batch.getId());
					
					incrementAnomaly();
					
					LOG.error("Bug(Anomalie) généré(e) pour l'évènement {}", Event.find(event.getId()).getSubject());
					
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

		String comment = "Compte rendu de la génération de rappel des évènements :\n";
		comment += String.format("\t* %s Evénment(s) traité(s)\n", batch.getDone());
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());
		
		super.stop();
		addComment(comment);
		
	}

}
