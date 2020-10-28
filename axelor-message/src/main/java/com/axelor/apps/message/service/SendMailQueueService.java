/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.message.service;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JpaSupport;
import com.axelor.event.Observes;
import com.axelor.events.ShutdownEvent;
import com.axelor.exception.service.TraceBackService;
import com.axelor.mail.MailBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SendMailQueueService extends JpaSupport {

  private static final int ENTITY_FIND_TIMEOUT = 10000;
  private static final int ENTITY_FIND_INTERVAL = 200;
  protected MessageRepository messageRepository;

  @Inject
  public SendMailQueueService(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected ExecutorService executor = Executors.newSingleThreadExecutor();

  /**
   * Submit a mail job to an executor which will send mails in a separate thread.
   *
   * @param mailBuilder
   * @param message
   */
  public void submitMailJob(MailBuilder mailBuilder, Message message) {
    long messageId = message.getId();
    log.debug("Submitting job to executor for message {}...", messageId);
    executor.submit(
        () -> {
          try {
            final long startTime = System.currentTimeMillis();
            boolean done = false;
            PersistenceException persistenceException = null;
            log.debug("Sending message {}...", messageId);
            mailBuilder.send();
            log.debug("Message {} sent.", messageId);
            do {
              try {
                inTransaction(
                    () -> {
                      final Message updateMessage = findMessage(messageId);
                      getEntityManager().lock(updateMessage, LockModeType.PESSIMISTIC_WRITE);
                      updateMessage.setSentByEmail(true);
                      updateMessage.setStatusSelect(MessageRepository.STATUS_SENT);
                      updateMessage.setSentDateT(LocalDateTime.now());
                      updateMessage.setSenderUser(AuthUtils.getUser());
                      messageRepository.save(updateMessage);
                    });
                done = true;
              } catch (PersistenceException e) {
                persistenceException = e;
                sleep();
              }
            } while (!done && System.currentTimeMillis() - startTime < ENTITY_FIND_TIMEOUT);
            if (!done) {
              throw persistenceException;
            }
          } catch (Exception e) {
            log.debug("Exception when sending email", e);
            TraceBackService.trace(e);
          }
          return true;
        });
  }

  /**
   * This method calls shutdown on the executor when the application stops.
   *
   * @param event shutdown event
   */
  protected void onApplicationShutdown(@Observes ShutdownEvent event) {
    log.debug("Shutting down mail executor..");
    executor.shutdown();
  }

  private Message findMessage(Long messageId) {
    Message foundMessage;
    final long startTime = System.currentTimeMillis();

    while ((foundMessage = messageRepository.find(messageId)) == null
        && System.currentTimeMillis() - startTime < ENTITY_FIND_TIMEOUT) {
      sleep();
    }

    if (foundMessage == null) {
      throw new EntityNotFoundException(messageId.toString());
    }

    return foundMessage;
  }

  private void sleep() {
    try {
      Thread.sleep(ENTITY_FIND_INTERVAL);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
