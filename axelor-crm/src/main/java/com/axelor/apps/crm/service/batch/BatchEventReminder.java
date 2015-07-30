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

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.EventReminder;
import com.axelor.apps.crm.db.IEventReminder;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.message.MessageServiceCrmImpl;
import com.axelor.apps.crm.service.EventReminderService;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.apps.message.service.MessageService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class BatchEventReminder extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchEventReminder.class);

	private boolean stop = false;
	private LocalDateTime today;

	@Inject
	private EventService eventService;

	@Inject
	public BatchEventReminder(EventReminderService eventReminderService, MessageServiceCrmImpl messageServiceCrmImpl, MailAccountService mailAccountService) {

		super(eventReminderService, messageServiceCrmImpl, mailAccountService);
		this.today = Beans.get(GeneralService.class).getTodayDateTime().toLocalDateTime();
	}


	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {

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

			List<? extends EventReminder> eventReminderList = eventReminderService.all()
					.filter("self.event.startDateTime > ?1 AND self.durationTypeSelect = ?2", today, durationTypeSelect).fetch();


			for(EventReminder eventReminder : eventReminderList)  {

				try {

					eventReminder = eventReminderService.find(eventReminder.getId());

					if(this.isExpired(eventReminder, durationTypeSelect))  {
						eventReminder.setIsReminded(true);
						updateEventReminder(eventReminder);
						i++;
					}


				} catch (Exception e) {

					TraceBackService.trace(new Exception(String.format(I18n.get(IExceptionMessage.BATCH_EVENT_REMINDER_1),
							eventReminderService.find(eventReminder.getId()).getEvent().getSummary()), e), IException.CRM, batch.getId());

					incrementAnomaly();

					LOG.error("Bug(Anomalie) généré(e) pour le rappel de l'évènement {}", eventReminderService.find(eventReminder.getId()).getEvent().getSummary());

				} finally {

					if (i % 1 == 0) { JPA.clear(); }

				}
			}
		}
	}


	private boolean isExpired(EventReminder eventReminder, int durationTypeSelect)  {

		LocalDateTime startDateTime = eventReminder.getEvent().getStartDate();

		switch (durationTypeSelect) {
		case IEventReminder.DURATION_MINUTES:

			if((startDateTime.minusMinutes(eventReminder.getDuration())).isBefore(today))  {
				return true;
			}
			break;

		case IEventReminder.DURATION_HOURS:

			if((startDateTime.minusHours(eventReminder.getDuration())).isBefore(today))  {
				return true;
			}
			break;

		case IEventReminder.DURATION_DAYS:

			if((startDateTime.minusDays(eventReminder.getDuration())).isBefore(today))  {
				return true;
			}
			break;

		case IEventReminder.DURATION_WEEKS:

			if((startDateTime.minusWeeks(eventReminder.getDuration())).isBefore(today))  {
				return true;
			}
			break;
		}

		return false;

	}



	protected void generateMessageProcess() {

		if(!stop)  {

			int i = 0;

			Query q = JPA.em().createQuery("select event FROM EventReminder as er WHERE er.isReminded = true and ?1 MEMBER OF er.batchSet");
			q.setParameter(1, batch);

			@SuppressWarnings("unchecked")
			List<Event> eventList = q.getResultList();

			for(Event event : eventList)  {
				try {
					Message message = messageServiceCrmImpl.createMessage( event );
					message = Beans.get(MessageService.class).sendByEmail(message);
				} catch (Exception e) {

					TraceBackService.trace(new Exception(String.format(I18n.get("Event")+" %s",
							eventService.find(event.getId()).getSummary()), e), IException.CRM, batch.getId());

					incrementAnomaly();

					LOG.error("Bug(Anomalie) généré(e) pour l'évènement {}", eventService.find(event.getId()).getSummary());

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
		comment += String.format("\t* %s "+I18n.get(IExceptionMessage.BATCH_EVENT_REMINDER_3)+"\n", batch.getDone());
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

		super.stop();
		addComment(comment);

	}

}
