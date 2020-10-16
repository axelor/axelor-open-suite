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

import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.TemplateContext;
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
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaModel;
import com.axelor.rpc.Context;
import com.axelor.text.GroovyTemplates;
import com.axelor.text.StringTemplates;
import com.axelor.text.Templates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMessageServiceImpl implements TemplateMessageService {

  private static final String RECIPIENT_SEPARATOR = ";|,";
  private static final char TEMPLATE_DELIMITER = '$';

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MessageService messageService;
  protected TemplateContextService templateContextService;

  @Inject
  public TemplateMessageServiceImpl(
      MessageService messageService, TemplateContextService templateContextService) {
    this.messageService = messageService;
    this.templateContextService = templateContextService;
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
  @Transactional(rollbackOn = {Exception.class})
  public Message generateMessage(Long objectId, String model, String tag, Template template)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException {

    Templates templates;
    Map<String, Object> templatesContext = Maps.newHashMap();

    if (template.getTemplateEngineSelect() == TemplateRepository.TEMPLATE_ENGINE_GROOVY_TEMPLATE) {
      templates = Beans.get(GroovyTemplates.class);
    } else {
      templates = new StringTemplates(TEMPLATE_DELIMITER, TEMPLATE_DELIMITER);
    }

    Object modelObj = template.getIsJson() ? template.getMetaJsonModel() : template.getMetaModel();

    if (modelObj != null) {
      String modelName =
          template.getIsJson()
              ? ((MetaJsonModel) modelObj).getName()
              : ((MetaModel) modelObj).getFullName();

      if (!model.equals(modelName)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(IExceptionMessage.INVALID_MODEL_TEMPLATE_EMAIL), modelName, model));
      }
      initMaker(objectId, model, tag, template.getIsJson(), templatesContext);
      computeTemplateContexts(
          template.getTemplateContextList(),
          objectId,
          model,
          template.getIsJson(),
          templatesContext);
    }

    log.debug("model : {}", model);
    log.debug("tag : {}", tag);
    log.debug("object id : {}", objectId);
    log.debug("template : {}", template);

    String content = "";
    String subject = "";
    String replyToRecipients = "";
    String toRecipients = "";
    String ccRecipients = "";
    String bccRecipients = "";
    String addressBlock = "";
    int mediaTypeSelect;
    String signature = "";

    if (!Strings.isNullOrEmpty(template.getContent())) {
      content = templates.fromText(template.getContent()).make(templatesContext).render();
    }

    if (!Strings.isNullOrEmpty(template.getAddressBlock())) {
      addressBlock = templates.fromText(template.getAddressBlock()).make(templatesContext).render();
    }

    if (!Strings.isNullOrEmpty(template.getSubject())) {
      subject = templates.fromText(template.getSubject()).make(templatesContext).render();
      log.debug("Subject ::: {}", subject);
    }

    if (!Strings.isNullOrEmpty(template.getReplyToRecipients())) {
      replyToRecipients =
          templates.fromText(template.getReplyToRecipients()).make(templatesContext).render();
      log.debug("Reply to ::: {}", replyToRecipients);
    }

    if (template.getToRecipients() != null) {
      toRecipients = templates.fromText(template.getToRecipients()).make(templatesContext).render();
      log.debug("To ::: {}", toRecipients);
    }

    if (template.getCcRecipients() != null) {
      ccRecipients = templates.fromText(template.getCcRecipients()).make(templatesContext).render();
      log.debug("CC ::: {}", ccRecipients);
    }

    if (template.getBccRecipients() != null) {
      bccRecipients =
          templates.fromText(template.getBccRecipients()).make(templatesContext).render();
      log.debug("BCC ::: {}", bccRecipients);
    }

    mediaTypeSelect = this.getMediaTypeSelect(template);
    log.debug("Media ::: {}", mediaTypeSelect);

    if (template.getSignature() != null) {
      signature = templates.fromText(template.getSignature()).make(templatesContext).render();
      log.debug("Signature ::: {}", signature);
    }
    EmailAccount mailAccount = getMailAccount();
    Message message =
        messageService.createMessage(
            model,
            Math.toIntExact(objectId),
            subject,
            content,
            getEmailAddress(mailAccount.getFromAddress()),
            getEmailAddresses(replyToRecipients),
            getEmailAddresses(toRecipients),
            getEmailAddresses(ccRecipients),
            getEmailAddresses(bccRecipients),
            null,
            addressBlock,
            mediaTypeSelect,
            mailAccount,
            signature);

    message.setTemplate(Beans.get(TemplateRepository.class).find(template.getId()));

    message = Beans.get(MessageRepository.class).save(message);

    messageService.attachMetaFiles(message, getMetaFiles(template, templates, templatesContext));

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
  public Set<MetaFile> getMetaFiles(
      Template template, Templates templates, Map<String, Object> templatesContext)
      throws AxelorException, IOException {

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
  public Map<String, Object> initMaker(
      long objectId, String model, String tag, boolean isJson, Map<String, Object> templatesContext)
      throws ClassNotFoundException {

    if (isJson) {
      templatesContext.put(tag, JPA.find(MetaJsonRecord.class, objectId));
    } else {
      Class<? extends Model> myClass = (Class<? extends Model>) Class.forName(model);
      templatesContext.put(tag, JPA.find(myClass, objectId));
    }

    return templatesContext;
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> computeTemplateContexts(
      List<TemplateContext> templateContextList,
      long objectId,
      String model,
      boolean isJson,
      Map<String, Object> templatesContext)
      throws ClassNotFoundException {

    if (templateContextList == null) {
      return templatesContext;
    }

    Context context = null;
    if (isJson) {
      context = new com.axelor.rpc.Context(objectId, MetaJsonRecord.class);
    } else {
      Class<? extends Model> myClass = (Class<? extends Model>) Class.forName(model);
      context = new com.axelor.rpc.Context(objectId, myClass);
    }

    for (TemplateContext templateContext : templateContextList) {
      Object result = templateContextService.computeTemplateContext(templateContext, context);
      templatesContext.put(templateContext.getName(), result);
    }

    return templatesContext;
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
      Map<String, Object> values = new HashMap<>();
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
      return mailAccount;
    }

    return null;
  }
}
