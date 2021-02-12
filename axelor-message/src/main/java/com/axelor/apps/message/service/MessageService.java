/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaAttachment;
import com.axelor.meta.db.MetaFile;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.mail.MessagingException;
import wslite.json.JSONException;

public interface MessageService {

  @Transactional
  public Message createMessage(
      String model,
      long id,
      String subject,
      String content,
      EmailAddress fromEmailAddress,
      List<EmailAddress> replytoEmailAddressList,
      List<EmailAddress> toEmailAddressList,
      List<EmailAddress> ccEmailAddressList,
      List<EmailAddress> bccEmailAddressList,
      Set<MetaFile> metaFiles,
      String addressBlock,
      int mediaTypeSelect,
      EmailAccount emailAccount,
      String signature);

  @Transactional(rollbackOn = {Exception.class})
  /**
   * Function is used to create temporary {@link Message}, which will only be send but not be saved.
   * <br>
   * Only when isTemporaryMessage = {@code True}.
   *
   * @param model
   * @param id
   * @param subject
   * @param content
   * @param fromEmailAddress
   * @param replyToEmailAddressList
   * @param toEmailAddressList
   * @param ccEmailAddressList
   * @param bccEmailAddressList
   * @param metaFiles
   * @param addressBlock
   * @param mediaTypeSelect
   * @param emailAccount
   * @param signature
   * @param isTemporaryMessage
   * @return
   */
  public Message createMessage(
      String model,
      long id,
      String subject,
      String content,
      EmailAddress fromEmailAddress,
      List<EmailAddress> replyToEmailAddressList,
      List<EmailAddress> toEmailAddressList,
      List<EmailAddress> ccEmailAddressList,
      List<EmailAddress> bccEmailAddressList,
      Set<MetaFile> metaFiles,
      String addressBlock,
      int mediaTypeSelect,
      EmailAccount emailAccount,
      String signature,
      Boolean isTemporaryMessage);

  @Transactional
  public void attachMetaFiles(Message message, Set<MetaFile> metaFiles);

  public Set<MetaAttachment> getMetaAttachments(Message message);

  /**
   * Send {@link Message}.
   *
   * @param message
   * @return
   * @throws AxelorException
   */
  public Message sendMessage(Message message) throws AxelorException, JSONException, IOException;

  /**
   * Send {@link Message}.
   *
   * <p>If @param isTemporaryEmail is {@code True}, Message will not saved but only send.
   *
   * <p>
   *
   * @param message
   * @param isTemporaryEmail
   * @return
   * @throws AxelorException
   * @throws MessagingException
   */
  public Message sendMessage(Message message, Boolean isTemporaryEmail)
      throws AxelorException, MessagingException, JSONException, IOException;

  /**
   * Send {@link Message} as Email.
   *
   * @param message
   * @return
   * @throws MessagingException
   * @throws AxelorException
   */
  public Message sendByEmail(Message message) throws MessagingException, AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  /**
   * Send Message as email.
   *
   * <p>If @param isTemporaryEmail is {@code True}, Message will not saved but only send.
   *
   * <p>
   *
   * @param message
   * @param isTemporaryEmail
   * @return
   * @throws MessagingException
   * @throws AxelorException
   */
  public Message sendByEmail(Message message, Boolean isTemporaryEmail)
      throws MessagingException, AxelorException;

  @Transactional
  public Message sendToUser(Message message);

  @Transactional(rollbackOn = {Exception.class})
  public Message sendByMail(Message message);

  @Transactional(rollbackOn = {Exception.class})
  public Message sendSMS(Message message) throws AxelorException, IOException, JSONException;

  public String printMessage(Message message) throws AxelorException;

  /**
   * Regenerate message with template attached it.
   *
   * @param message Message to regenerate.
   * @return The new message regenerated.
   * @throws Exception If a error append during generation.
   */
  Message regenerateMessage(Message message) throws Exception;

  public String getFullEmailAddress(EmailAddress emailAddress);
}
