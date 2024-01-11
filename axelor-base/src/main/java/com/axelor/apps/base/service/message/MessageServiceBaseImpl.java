/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.message;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ModelEmailLink;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.repo.ModelEmailLinkRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.axelor.message.db.EmailAccount;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.AppSettingsMessageService;
import com.axelor.message.service.MessageServiceImpl;
import com.axelor.message.service.SendMailQueueService;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.axelor.studio.db.AppBase;
import com.axelor.text.StringTemplates;
import com.axelor.text.Templates;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;

public class MessageServiceBaseImpl extends MessageServiceImpl implements MessageBaseService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected final UserService userService;
  protected final AppBaseService appBaseService;

  @Inject
  public MessageServiceBaseImpl(
      MetaAttachmentRepository metaAttachmentRepository,
      MessageRepository messageRepository,
      SendMailQueueService sendMailQueueService,
      AppSettingsMessageService appSettingsMessageService,
      UserService userService,
      AppBaseService appBaseService) {
    super(
        metaAttachmentRepository,
        messageRepository,
        sendMailQueueService,
        appSettingsMessageService);
    this.userService = userService;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional
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
    this.manageRelatedTo(message);

    return messageRepository.save(message);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void manageRelatedTo(Message message) {

    AppBase appBase = appBaseService.getAppBase();
    if (ObjectUtils.isEmpty(appBase.getEmailLinkList())) {
      return;
    }

    EmailAddress fromEmailAddress = message.getFromEmailAddress();
    Set<EmailAddress> toEmailAddressList = message.getToEmailAddressSet();
    List<String> emailAddresses = null;
    if (ObjectUtils.notEmpty(toEmailAddressList)) {
      emailAddresses =
          toEmailAddressList.stream().map(EmailAddress::getAddress).collect(Collectors.toList());
    }

    if (appBase.getManageCcBccRelatedTo()) {
      Set<EmailAddress> ccEmailAddressList = message.getCcEmailAddressSet();
      Set<EmailAddress> bccEmailAddressList = message.getBccEmailAddressSet();
      if (ObjectUtils.notEmpty(ccEmailAddressList)) {
        emailAddresses.addAll(
            ccEmailAddressList.stream().map(EmailAddress::getAddress).collect(Collectors.toList()));
      }
      if (ObjectUtils.notEmpty(bccEmailAddressList)) {
        emailAddresses.addAll(
            bccEmailAddressList.stream()
                .map(EmailAddress::getAddress)
                .collect(Collectors.toList()));
      }
    }

    for (ModelEmailLink modelEmailLink : appBase.getEmailLinkList()) {
      try {
        String className = modelEmailLink.getMetaModel().getFullName();
        Class<Model> klass = (Class<Model>) Class.forName(className);
        List<Model> relatedRecords = new ArrayList<>();

        if (modelEmailLink.getAddressTypeSelect() == ModelEmailLinkRepository.ADDRESS_TYPE_FROM
            && fromEmailAddress != null
            && StringUtils.notBlank(fromEmailAddress.getAddress())) {
          relatedRecords.addAll(
              JPA.all(klass)
                  .filter(String.format("self.%s = :email", modelEmailLink.getEmailField()))
                  .bind("email", fromEmailAddress.getAddress())
                  .cacheable()
                  .fetch());
        }

        if (modelEmailLink.getAddressTypeSelect() == ModelEmailLinkRepository.ADDRESS_TYPE_TO
            && ObjectUtils.notEmpty(emailAddresses)) {
          relatedRecords.addAll(
              JPA.all(klass)
                  .filter(String.format("self.%s IN :emails", modelEmailLink.getEmailField()))
                  .bind("emails", emailAddresses)
                  .cacheable()
                  .fetch());
        }

        for (Model relatedRecord : relatedRecords) {
          addMessageRelatedTo(message, className, relatedRecord.getId());
        }
      } catch (Exception e) {
        TraceBackService.trace(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public String printMessage(Message message) {

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

    try {
      return Beans.get(TemplateMessageServiceBaseImpl.class)
          .generateBirtTemplateLink(
              templates,
              templatesContext,
              fileName,
              birtTemplate.getTemplateLink(),
              birtTemplate.getFormat(),
              birtTemplate.getBirtTemplateParameterList());

    } catch (AxelorException e) {
      TraceBackService.trace(e);
      throw new IllegalStateException(e);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendByEmail(Message message) throws MessagingException {

    if (appBaseService.getAppBase().getActivateSendingEmail()) {
      message.setStatusSelect(MessageRepository.STATUS_IN_PROGRESS);
      return super.sendByEmail(message);
    }
    return message;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message sendSMS(Message message) throws IOException, JSONException {

    if (appBaseService.getAppBase().getActivateSendingEmail()) {
      return super.sendSMS(message);
    }
    return message;
  }

  @Override
  protected String getSender(Message message) {
    return message.getCompany().getCode();
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
      partnerName = emailAddress.getPartner().getSimpleFullName();
    }

    return "\"" + partnerName + "\" <" + emailAddress.getAddress() + ">";
  }
}
