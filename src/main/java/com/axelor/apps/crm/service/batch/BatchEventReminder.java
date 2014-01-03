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

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.db.EventReminder;
import com.axelor.apps.crm.db.IEventReminder;
import com.axelor.apps.crm.service.EventReminderService;
import com.axelor.apps.crm.service.EventReminderThread;
import com.axelor.db.JPA;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Injector;

public class BatchEventReminder extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchEventReminder.class);

	private boolean stop = false;
	private LocalDateTime today;
	
	private Injector injector;
	
	@Inject
	public BatchEventReminder(EventReminderService eventReminderService) {
		
		super(eventReminderService);
		this.today = GeneralService.getTodayDateTime().toLocalDateTime();
	}
	
	
	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException {
		
		super.start();
		
	}

	
	@Override
	protected void process() {
	
		this.markEventReminderProcess();
		this.generateMessageProcess();
		
	}
	
	
	protected void markEventReminderProcess() {
	
		if(!stop)  {
			
			int i = 0;
			
			int durationTypeSelect = batch.getCrmBatch().getDurationTypeSelect();
			
			List<EventReminder> eventReminderList = EventReminder.all()
					.filter("self.event.startDateTime > ?1 AND self.durationTypeSelect = ?2", today, durationTypeSelect).fetch();
			
			
			for(EventReminder eventReminder : eventReminderList)  {
				
				try {
					
					eventReminder = EventReminder.find(eventReminder.getId());
					
					if(this.isExpired(eventReminder, durationTypeSelect))  {
						eventReminder.setIsReminded(true);
						updateEventReminder(eventReminder);
						i++;
					}
					
					
				} catch (Exception e) {
					
					TraceBackService.trace(new Exception(String.format("Event reminder %s", 
							EventReminder.find(eventReminder.getId()).getEvent().getSubject()), e), IException.CRM, batch.getId());
					
					incrementAnomaly();
					
					LOG.error("Bug(Anomalie) généré(e) pour le rappel de l'évènement {}", EventReminder.find(eventReminder.getId()).getEvent().getSubject());
					
				} finally {
					
					if (i % 1 == 0) { JPA.clear(); }
		
				}	
			}
		}
	}
	
	
	private boolean isExpired(EventReminder eventReminder, int durationTypeSelect)  {
		
		LocalDateTime startDateTime = eventReminder.getEvent().getStartDateTime();
		
		switch (durationTypeSelect) {
		case IEventReminder.DURATION_MINUTES:
			
			if((startDateTime.minusMinutes(eventReminder.getDuration())).isBefore(today))  {
				return true;
			}
			
		case IEventReminder.DURATION_HOURS:
								
			if((startDateTime.minusHours(eventReminder.getDuration())).isBefore(today))  {
				return true;
			}
								
		case IEventReminder.DURATION_DAYS:
			
			if((startDateTime.minusDays(eventReminder.getDuration())).isBefore(today))  {
				return true;
			}
			
		case IEventReminder.DURATION_WEEKS:
			
			if((startDateTime.minusWeeks(eventReminder.getDuration())).isBefore(today))  {
				return true;
			}

		default:
			return false;
		}
		
	}
	
	
	
	protected void generateMessageProcess() {
		
		if(!stop)  {
			EventReminderThread thread = new EventReminderThread(Batch.find(batch.getId()), injector);
			thread.start();
		}
	}
	
	
	

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = "Compte rendu de la génération de rappel des évènements :\n";
		comment += String.format("\t* %s Rappel(s) traité(s)\n", batch.getDone());
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());
		
		super.stop();
		addComment(comment);
		
	}

}
