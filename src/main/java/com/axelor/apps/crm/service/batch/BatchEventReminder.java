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
