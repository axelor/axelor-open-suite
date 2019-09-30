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
package com.axelor.apps.message.service;

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.dms.db.DMSFile;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMessageServiceImpl implements TemplateMessageService {

  private static final String RECIPIENT_SEPARATOR = ";|,";
  private static final char TEMPLATE_DELIMITER = '$';

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected TemplateMaker maker =
      new TemplateMaker(Locale.FRENCH, TEMPLATE_DELIMITER, TEMPLATE_DELIMITER);

  protected MessageService messageService;

  @Inject
  public TemplateMessageServiceImpl(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public Message generateMessage(Model model, Template template)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException {
    Class<?> klass = EntityHelper.getEntityClass(model);
    return generateMessage(
        model.getId(), klass.getCanonicalName(), klass.getSimpleName(), template);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Message generateMessage(Long objectId, String model, String tag, Template template)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException {

    MetaModel metaModel = template.getMetaModel();
    if (metaModel != null) {
      if (!model.equals(metaModel.getFullName())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.INVALID_MODEL_TEMPLATE_EMAIL),
                metaModel.getFullName(),
                model));
      }
      initMaker(objectId, model, tag);
    }

    log.debug("model : {}", model);
    log.debug("tag : {}", tag);
    log.debug("object id : {}", objectId);
    log.debug("template : {}", template);

    String content = "",
        subject = "",
        from = "",
        replyToRecipients = "",
        toRecipients = "",
        ccRecipients = "",
        bccRecipients = "",
        addressBlock = "";
    int mediaTypeSelect;

    if (!Strings.isNullOrEmpty(template.getContent())) {
      // Set template
      maker.setTemplate(template.getContent());
      content = maker.make();
    }

    if (!Strings.isNullOrEmpty(template.getAddressBlock())) {
      maker.setTemplate(template.getAddressBlock());
      // Make it
      addressBlock = maker.make();
    }

    if (!Strings.isNullOrEmpty(template.getSubject())) {
      maker.setTemplate(template.getSubject());
      subject = maker.make();
      log.debug("Subject ::: {}", subject);
    }

    if (!Strings.isNullOrEmpty(template.getFromAdress())) {
      maker.setTemplate(template.getFromAdress());
      from = maker.make();
      log.debug("From ::: {}", from);
    }

    if (!Strings.isNullOrEmpty(template.getReplyToRecipients())) {
      maker.setTemplate(template.getReplyToRecipients());
      replyToRecipients = maker.make();
      log.debug("Reply to ::: {}", replyToRecipients);
    }

    if (template.getToRecipients() != null) {
      maker.setTemplate(template.getToRecipients());
      toRecipients = maker.make();
      log.debug("To ::: {}", toRecipients);
    }

    if (template.getCcRecipients() != null) {
      maker.setTemplate(template.getCcRecipients());
      ccRecipients = maker.make();
      log.debug("CC ::: {}", ccRecipients);
    }

    if (template.getBccRecipients() != null) {
      maker.setTemplate(template.getBccRecipients());
      bccRecipients = maker.make();
      log.debug("BCC ::: {}", bccRecipients);
    }

    mediaTypeSelect = this.getMediaTypeSelect(template);
    log.debug("Media ::: {}", mediaTypeSelect);
    log.debug("Content ::: {}", content);

    Message message =
        messageService.createMessage(
            model,
            Long.valueOf(objectId).intValue(),
            subject,
            content,
            getEmailAddress(from),
            getEmailAddresses(replyToRecipients),
            getEmailAddresses(toRecipients),
            getEmailAddresses(ccRecipients),
            getEmailAddresses(bccRecipients),
            null,
            addressBlock,
            mediaTypeSelect,
            getMailAccount());

    message.setTemplate(Beans.get(TemplateRepository.class).find(template.getId()));

    message = Beans.get(MessageRepository.class).save(message);

    messageService.attachMetaFiles(message, getMetaFiles(template));

    return message;
  }

  @Override
  public Message generateAndSendMessage(Model model, Template template)
      throws MessagingException, IOException, AxelorException, ClassNotFoundException,
          InstantiationException, IllegalAccessException {

    Message message = this.generateMessage(model, template);
    messageService.sendMessage(message);

    return message;
  }

  @Override
  public Set<MetaFile> getMetaFiles(Template template) throws AxelorException, IOException {

    List<DMSFile> metaAttachments =
        Query.of(DMSFile.class)
            .filter(
                "self.relatedId = ?1 AND self.relatedModel = ?2",
                template.getId(),
                EntityHelper.getEntityClass(template).getName())
            .fetch();
    Set<MetaFile> metaFiles = Sets.newHashSet();
    for (DMSFile metaAttachment : metaAttachments) {
      if (!metaAttachment.getIsDirectory()) metaFiles.add(metaAttachment.getMetaFile());
    }

    log.debug("Metafile to attach: {}", metaFiles);
    return metaFiles;
  }

  @Override
  @SuppressWarnings("unchecked")
  public TemplateMaker initMaker(long objectId, String model, String tag)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {

    Class<? extends Model> myClass = (Class<? extends Model>) Class.forName(model);
    maker.setContext(JPA.find(myClass, objectId), tag);

    return maker;
  }

  protected List<EmailAddress> getEmailAddresses(String recipients) {

    List<EmailAddress> emailAddressList = Lists.newArrayList();
    if (Strings.isNullOrEmpty(recipients)) {
      return emailAddressList;
    }

    for (String recipient :
        Splitter.onPattern(RECIPIENT_SEPARATOR)
            .trimResults()
            .omitEmptyStrings()
            .splitToList(recipients)) {
      emailAddressList.add(getEmailAddress(recipient));
    }
    return emailAddressList;
  }

  protected EmailAddress getEmailAddress(String recipient) {

    if (Strings.isNullOrEmpty(recipient)) {
      return null;
    }

    EmailAddressRepository emailAddressRepo = Beans.get(EmailAddressRepository.class);

    EmailAddress emailAddress = emailAddressRepo.findByAddress(recipient);

    if (emailAddress == null) {
      Map<String, Object> values = new HashMap<String, Object>();
      values.put("address", recipient);
      emailAddress = emailAddressRepo.create(values);
    }

    return emailAddress;
  }

  protected Integer getMediaTypeSelect(Template template) {

    return template.getMediaTypeSelect();
  }

  protected EmailAccount getMailAccount() {

    EmailAccount mailAccount = Beans.get(MailAccountService.class).getDefaultSender();

    if (mailAccount != null) {
      log.debug("Email account ::: {}", mailAccount);
      return mailAccount;
    }

    return null;
  }
}
