/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.crm.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.EventReminder;
import com.axelor.apps.crm.db.repo.EventReminderRepository;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.message.MessageServiceCrmImpl;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.EmailAddressRepository;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.MailAccountService;
import com.axelor.message.service.MessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
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

  @Inject private EmailAddressRepository emailAddressRepo;

  @Inject
  public BatchEventReminder(
      MessageServiceCrmImpl messageServiceCrmImpl, MailAccountService mailAccountService) {

    super(messageServiceCrmImpl, mailAccountService);
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

          Integer eventStatusSelect = eventReminder.getEvent().getStatusSelect();
          boolean eventIsNotFinished = eventStatusSelect == EventRepository.STATUS_PLANNED;
          if (!eventReminder.getIsReminded() && isExpired(eventReminder) && eventIsNotFinished) {
            updateEventReminder(eventReminder);
            i++;
          }

        } catch (Exception e) {

          TraceBackService.trace(
              new Exception(
                  String.format(
                      I18n.get(CrmExceptionMessage.BATCH_EVENT_REMINDER_1),
                      eventReminderRepo.find(eventReminder.getId()).getEvent().getSubject()),
                  e),
              ExceptionOriginRepository.CRM,
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

  protected boolean isExpired(EventReminder eventReminder) {

    LocalDateTime startDateTime = eventReminder.getEvent().getStartDateTime();

    if (EventReminderRepository.MODE_AT_DATE.equals(eventReminder.getModeSelect())) {
      return eventReminder
          .getSendingDateT()
          .isBefore(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
    } else { // defaults to EventReminderRepository.MODE_BEFORE_DATE
      int durationTypeSelect = eventReminder.getDurationTypeSelect();
      switch (durationTypeSelect) {
        case EventReminderRepository.DURATION_TYPE_MINUTES:
          if ((startDateTime.minusMinutes(eventReminder.getDuration()))
              .isBefore(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime())) {
            return true;
          }
          break;

        case EventReminderRepository.DURATION_TYPE_HOURS:
          if ((startDateTime.minusHours(eventReminder.getDuration()))
              .isBefore(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime())) {
            return true;
          }
          break;

        case EventReminderRepository.DURATION_TYPE_DAYS:
          if ((startDateTime.minusDays(eventReminder.getDuration()))
              .isBefore(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime())) {
            return true;
          }
          break;

        case EventReminderRepository.DURATION_TYPE_WEEKS:
          if ((startDateTime.minusWeeks(eventReminder.getDuration()))
              .isBefore(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime())) {
            return true;
          }
          break;
      }
    }

    return false;
  }

  protected void generateMessageProcess() {

    MessageRepository messageRepo = Beans.get(MessageRepository.class);

    if (!stop) {

      int i = 0;

      Query q =
          JPA.em()
              .createQuery(
                  " SELECT er FROM EventReminder as er WHERE er.isReminded = true and ?1 MEMBER OF er.batchSet");
      q.setParameter(1, batch);

      @SuppressWarnings("unchecked")
      List<EventReminder> eventReminderList = q.getResultList();

      for (EventReminder eventReminder : eventReminderList) {
        try {
          eventReminder = eventReminderRepo.find(eventReminder.getId());
          Message message = messageServiceCrmImpl.createMessage(eventReminder.getEvent());

          // Send reminder to owner of the reminder in any case
          if (eventReminder.getUser().getPartner() != null
              && eventReminder.getUser().getPartner().getEmailAddress() != null) {
            message.addToEmailAddressSetItem(
                eventReminder.getUser().getPartner().getEmailAddress());
          } else if (eventReminder.getUser().getEmail() != null) {
            message.addToEmailAddressSetItem(
                findOrCreateEmailAddress(
                    eventReminder.getUser().getEmail(),
                    "[" + eventReminder.getUser().getEmail() + "]"));
          } else {
            messageRepo.remove(message);
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(CrmExceptionMessage.CRM_CONFIG_USER_EMAIL),
                eventReminder.getUser().getName());
          }

          // Also send to attendees if needed
          if (EventReminderRepository.ASSIGN_TO_ALL.equals(eventReminder.getAssignToSelect())
              && eventReminder.getEvent().getAttendees() != null) {
            for (ICalendarUser iCalUser : eventReminder.getEvent().getAttendees()) {
              if (iCalUser.getUser() != null && iCalUser.getUser().getPartner() != null) {
                message.addToEmailAddressSetItem(iCalUser.getUser().getPartner().getEmailAddress());
              } else {
                message.addToEmailAddressSetItem(
                    findOrCreateEmailAddress(iCalUser.getEmail(), iCalUser.getName()));
              }
            }
          }

          message = Beans.get(MessageService.class).sendByEmail(message);
        } catch (Exception e) {

          TraceBackService.trace(
              new Exception(
                  String.format(
                      I18n.get("Event") + " %s",
                      eventRepo.find(eventReminder.getEvent().getId()).getSubject()),
                  e),
              ExceptionOriginRepository.CRM,
              batch.getId());

          incrementAnomaly();

          LOG.error(
              "Bug(Anomalie) généré(e) pour l'évènement {}",
              eventRepo.find(eventReminder.getEvent().getId()).getSubject());

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

    String comment = I18n.get(CrmExceptionMessage.BATCH_EVENT_REMINDER_2) + "\n";
    comment +=
        String.format(
            "\t* %s " + I18n.get(CrmExceptionMessage.BATCH_EVENT_REMINDER_3) + "\n",
            batch.getDone());
    comment +=
        String.format("\t" + I18n.get(BaseExceptionMessage.BASE_BATCH_3), batch.getAnomaly());

    super.stop();
    addComment(comment);
  }

  @Transactional
  protected EmailAddress findOrCreateEmailAddress(String email, String name) {
    EmailAddress emailAddress =
        emailAddressRepo.all().filter("self.name = '" + name + "'").fetchOne();

    if (emailAddress == null) {
      emailAddress = new EmailAddress();
      emailAddress.setAddress(email);
      emailAddress = emailAddressRepo.save(emailAddress);
    }

    return emailAddress;
  }
}
