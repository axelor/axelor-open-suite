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
package com.axelor.apps.projectdms.service;

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MailAccountService;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.projectdms.exception.IExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.dms.db.DMSFile;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;

public class DMSFileServiceImpl implements DMSFileService {

  private static final String SUBJECT = "Request for unlock document";
  private static final String CONTENT_EN =
      "Hello %s, <br/><br/>"
          + "I need to change document. Please unlock document. <br/><br/>"
          + "Regards,<br/>"
          + "%s";

  @Inject private MessageService messageService;
  @Inject private MailAccountService mailAccountService;
  @Inject private EmailAddressRepository emailAddressRepo;

  @Override
  public void sendEmail(DMSFile dmsFile) throws AxelorException, MessagingException {

    final EmailAccount emailAccount = mailAccountService.getDefaultSender();

    User currentUser = AuthUtils.getUser();
    User lockedBy = dmsFile.getLockedBy();
    EmailAddress emailAddress = this.getEmailAddress(lockedBy);

    String content = null;
    String language = currentUser.getLanguage();

    if (language != null && language.equals("fr")) {
      content = String.format(CONTENT_EN, lockedBy.getFullName(), currentUser.getFullName());
    } else {
      content = String.format(CONTENT_EN, lockedBy.getFullName(), currentUser.getFullName());
    }

    List<EmailAddress> toEmailAddressList = new ArrayList<>();
    toEmailAddressList.add(emailAddress);

    Message message =
        messageService.createMessage(
            null,
            0,
            I18n.get(SUBJECT),
            content,
            null,
            null,
            toEmailAddressList,
            null,
            null,
            null,
            null,
            MessageRepository.MEDIA_TYPE_EMAIL,
            emailAccount,
            null);

    messageService.sendByEmail(message);
  }

  private EmailAddress getEmailAddress(User user) throws AxelorException {

    EmailAddress emailAddress = null;

    if (user.getPartner() != null && user.getPartner().getEmailAddress() != null) {
      emailAddress = user.getPartner().getEmailAddress();
    } else if (!Strings.isNullOrEmpty(user.getEmail())) {
      emailAddress = emailAddressRepo.findByAddress(user.getEmail());

      if (emailAddress == null) {
        emailAddress = new EmailAddress(user.getEmail());
      }
    }

    if (emailAddress == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.LOCKED_BY_USER_EMAIL_NOT_CONFIGURED),
          user.getCode());
    }

    return emailAddress;
  }
}
