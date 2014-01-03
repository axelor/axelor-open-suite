/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
