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
package com.axelor.apps.base.service.message;

import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageServiceImpl;
import com.axelor.apps.message.service.SendMailQueueService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.axelor.text.StringTemplates;
import com.axelor.text.Templates;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageServiceBaseImpl extends MessageServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected UserService userService;
  protected AppBaseService appBaseService;

  @Inject
  public MessageServiceBaseImpl(
      MetaAttachmentRepository metaAttachmentRepository,
      MessageRepository messageRepository,
      SendMailQueueService sendMailQueueService,
      UserService userService,
      AppBaseService appBaseService) {
    super(metaAttachmentRepository, messageRepository, sendMailQueueService);
    this.userService = userService;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional
  public Message createMessage(
      String model,
      int id,
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
      String signature) {

    Message message =
        super.createMessage(
            model,
            id,
            subject,
            content,
            fromEmailAddress,
            replyToEmailAddressList,
            toEmailAddressList,
            ccEmailAddressList,
            bccEmailAddressList,
            metaFiles,
            addressBlock,
            mediaTypeSelect,
            emailAccount,
            signature);

    message.setSenderUser(AuthUtils.getUser());
    message.setCompany(userService.getUserActiveCompany());

    return messageRepository.save(message);
  }

  @SuppressWarnings("unchecked")
  @Override
  public String printMessage(Message message) throws AxelorException {

    Company company = message.getCompany();
    if (company == null) {
      return null;
    }

    PrintingSettings printSettings = company.getPrintingSettings();
    if (printSettings == null || printSettings.getDefaultMailBirtTemplate() == null) {
      return null;
    }

    BirtTemplate birtTemplate = printSettings.getDefaultMailBirtTemplate();

    logger.debug("Default BirtTemplate : {}", birtTemplate);

    Templates templates = new StringTemplates('$', '$');
    Map<String, Object> templatesContext = Maps.newHashMap();
    try {
      Class<? extends Model> className =
          (Class<? extends Model>) Class.forName(message.getClass().getName());
      templatesContext.put("Message", JPA.find(className, message.getId()));
    } catch (ClassNotFoundException e) {
      TraceBackService.trace(e);
    }

    String fileName =
        "Message "
            + message.getSubject()
            + "-"
            + appBaseService.getTodayDate(company).format(DateTimeFormatter.BASIC_ISO_DATE);

    return Beans.get(TemplateMessageServiceBaseImpl.class)
        .generateBirtTemplateLink(
            templates,
            templatesContext,
            fileName,
            birtTemplate.getTemplateLink(),
            birtTemplate.getFormat(),
            birtTemplate.getBirtTemplateParameterList());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendByEmail(Message message) throws MessagingException, AxelorException {

    if (Beans.get(AppBaseService.class).getAppBase().getActivateSendingEmail()) {
      message.setStatusSelect(MessageRepository.STATUS_IN_PROGRESS);
      return super.sendByEmail(message);
    }
    return message;
  }

  public List<String> getEmailAddressNames(Set<EmailAddress> emailAddressSet) {

    List<String> recipients = Lists.newArrayList();
    if (emailAddressSet != null) {
      for (EmailAddress emailAddress : emailAddressSet) {

        if (Strings.isNullOrEmpty(emailAddress.getName())) {
          continue;
        }
        recipients.add(emailAddress.getName());
      }
    }

    return recipients;
  }

  public String getToRecipients(Message message) {

    if (message.getToEmailAddressSet() != null && !message.getToEmailAddressSet().isEmpty()) {
      return Joiner.on(", \n").join(this.getEmailAddressNames(message.getToEmailAddressSet()));
    }

    return "";
  }

  @Override
  public String getFullEmailAddress(EmailAddress emailAddress) {
    String partnerName = "";
    if (emailAddress.getPartner() != null) {
      partnerName =
          new String(emailAddress.getPartner().getName().getBytes(), StandardCharsets.ISO_8859_1);
    }

    return "\"" + partnerName + "\" <" + emailAddress.getAddress() + ">";
  }
}
