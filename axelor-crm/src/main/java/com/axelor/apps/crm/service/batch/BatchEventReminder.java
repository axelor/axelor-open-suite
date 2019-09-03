/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.EventReminder;
import com.axelor.apps.crm.db.IEventReminder;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.message.MessageServiceCrmImpl;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.apps.message.service.MessageService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchEventReminder extends BatchStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private boolean stop = false;

  @Inject private EventRepository eventRepo;

  @Inject
  public BatchEventReminder(
      MessageServiceCrmImpl messageServiceCrmImpl, MailAccountService mailAccountService) {

    super(messageServiceCrmImpl, mailAccountService);
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

    if (!stop) {

      int i = 0;

      List<? extends EventReminder> eventReminderList = eventReminderRepo.all().fetch();

      for (EventReminder eventReminder : eventReminderList) {

        try {

          eventReminder = eventReminderRepo.find(eventReminder.getId());

          if (this.isExpired(eventReminder)) {
            eventReminder.setIsReminded(true);
            updateEventReminder(eventReminder);
            i++;
          }

        } catch (Exception e) {

          TraceBackService.trace(
              new Exception(
                  String.format(
                      I18n.get(IExceptionMessage.BATCH_EVENT_REMINDER_1),
                      eventReminderRepo.find(eventReminder.getId()).getEvent().getSubject()),
                  e),
              IException.CRM,
              batch.getId());

          incrementAnomaly();

          LOG.error(
              "Bug(Anomalie) généré(e) pour le rappel de l'évènement {}",
              eventReminderRepo.find(eventReminder.getId()).getEvent().getSubject());

        } finally {

          if (i % 1 == 0) {
            JPA.clear();
          }
        }
      }
    }
  }

  private boolean isExpired(EventReminder eventReminder) {

    LocalDateTime startDateTime = eventReminder.getEvent().getStartDateTime();
    int durationTypeSelect = eventReminder.getDurationTypeSelect();
    switch (durationTypeSelect) {
      case IEventReminder.DURATION_MINUTES:
        if ((startDateTime.minusMinutes(eventReminder.getDuration()))
            .isBefore(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime())) {
          return true;
        }
        break;

      case IEventReminder.DURATION_HOURS:
        if ((startDateTime.minusHours(eventReminder.getDuration()))
            .isBefore(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime())) {
          return true;
        }
        break;

      case IEventReminder.DURATION_DAYS:
        if ((startDateTime.minusDays(eventReminder.getDuration()))
            .isBefore(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime())) {
          return true;
        }
        break;

      case IEventReminder.DURATION_WEEKS:
        if ((startDateTime.minusWeeks(eventReminder.getDuration()))
            .isBefore(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime())) {
          return true;
        }
        break;
    }

    return false;
  }

  protected void generateMessageProcess() {

    if (!stop) {

      int i = 0;

      Query q =
          JPA.em()
              .createQuery(
                  "select event FROM EventReminder as er WHERE er.isReminded = true and ?1 MEMBER OF er.batchSet");
      q.setParameter(1, batch);

      @SuppressWarnings("unchecked")
      List<Event> eventList = q.getResultList();

      for (Event event : eventList) {
        try {
          Message message = messageServiceCrmImpl.createMessage(event);
          message = Beans.get(MessageService.class).sendByEmail(message);
        } catch (Exception e) {

          TraceBackService.trace(
              new Exception(
                  String.format(
                      I18n.get("Event") + " %s", eventRepo.find(event.getId()).getSubject()),
                  e),
              IException.CRM,
              batch.getId());

          incrementAnomaly();

          LOG.error(
              "Bug(Anomalie) généré(e) pour l'évènement {}",
              eventRepo.find(event.getId()).getSubject());

        } finally {

          if (i % 1 == 0) {
            JPA.clear();
          }
        }
      }
    }
  }

  /**
   * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the
   * entity in the persistant context. Warning : {@code batch} entity have to be saved before.
   */
  @Override
  protected void stop() {

    String comment = I18n.get(IExceptionMessage.BATCH_EVENT_REMINDER_2) + "\n";
    comment +=
        String.format(
            "\t* %s " + I18n.get(IExceptionMessage.BATCH_EVENT_REMINDER_3) + "\n", batch.getDone());
    comment +=
        String.format(
            "\t" + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
            batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
