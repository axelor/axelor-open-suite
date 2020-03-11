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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.BirtTemplateParameter;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateContextService;
import com.axelor.apps.message.service.TemplateMessageServiceImpl;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.tool.template.TemplateMaker;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import org.eclipse.birt.core.data.DataTypeUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMessageServiceBaseImpl extends TemplateMessageServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MessageServiceBase messageServiceBase;

  @Inject
  public TemplateMessageServiceBaseImpl(
      MessageService messageService,
      TemplateContextService templateContextService,
      MessageServiceBase messageServiceBase) {
    super(messageService, templateContextService);
    this.messageServiceBase = messageServiceBase;
  }

  @Override
  public Set<MetaFile> getMetaFiles(Template template) throws AxelorException, IOException {

    Set<MetaFile> metaFiles = super.getMetaFiles(template);
    if (template.getBirtTemplate() == null) {
      return metaFiles;
    }

    metaFiles.add(createMetaFileUsingBirtTemplate(maker, template.getBirtTemplate()));

    logger.debug("Metafile to attach: {}", metaFiles);

    return metaFiles;
  }

  public MetaFile createMetaFileUsingBirtTemplate(TemplateMaker maker, BirtTemplate birtTemplate)
      throws AxelorException, IOException {

    logger.debug("Generate birt metafile: {}", birtTemplate.getName());

    String fileName =
        birtTemplate.getName()
            + "-"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    File file =
        generateBirtTemplate(
            maker,
            fileName,
            birtTemplate.getTemplateLink(),
            birtTemplate.getFormat(),
            birtTemplate.getBirtTemplateParameterList());

    try (InputStream is = new FileInputStream(file)) {
      return Beans.get(MetaFiles.class).upload(is, fileName + "." + birtTemplate.getFormat());
    }
  }

  public File generateBirtTemplate(
      TemplateMaker maker,
      String fileName,
      String modelPath,
      String format,
      List<BirtTemplateParameter> birtTemplateParameterList)
      throws AxelorException {

    File birtTemplate = null;

    ReportSettings reportSettings =
        generateTemplate(maker, fileName, modelPath, format, birtTemplateParameterList);

    if (reportSettings != null) {
      birtTemplate = reportSettings.getFile();
    }

    return birtTemplate;
  }

  public String generateBirtTemplateLink(
      TemplateMaker maker,
      String fileName,
      String modelPath,
      String format,
      List<BirtTemplateParameter> birtTemplateParameterList)
      throws AxelorException {

    String birtTemplateFileLink = null;

    ReportSettings reportSettings =
        generateTemplate(maker, fileName, modelPath, format, birtTemplateParameterList);

    if (reportSettings != null) {
      birtTemplateFileLink = reportSettings.getFileLink();
    }

    return birtTemplateFileLink;
  }

  private ReportSettings generateTemplate(
      TemplateMaker maker,
      String fileName,
      String modelPath,
      String format,
      List<BirtTemplateParameter> birtTemplateParameterList)
      throws AxelorException {

    if (modelPath == null || modelPath.isEmpty()) {
      return null;
    }

    ReportSettings reportSettings =
        ReportFactory.createReport(modelPath, fileName).addFormat(format);

    for (BirtTemplateParameter birtTemplateParameter : birtTemplateParameterList) {
      maker.setTemplate(birtTemplateParameter.getValue());

      try {
        reportSettings.addParam(
            birtTemplateParameter.getName(),
            convertValue(birtTemplateParameter.getType(), maker.make()));
      } catch (BirtException e) {
        throw new AxelorException(
            e.getCause(),
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.TEMPLATE_MESSAGE_BASE_2));
      }
    }

    reportSettings.generate();
    return reportSettings;
  }

  private Object convertValue(String type, String value) throws BirtException {

    if (DesignChoiceConstants.PARAM_TYPE_BOOLEAN.equals(type)) {
      return DataTypeUtil.toBoolean(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_DATETIME.equals(type)) {
      return DataTypeUtil.toDate(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_DATE.equals(type)) {
      return DataTypeUtil.toSqlDate(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_TIME.equals(type)) {
      return DataTypeUtil.toSqlTime(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_DECIMAL.equals(type)) {
      return DataTypeUtil.toBigDecimal(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_FLOAT.equals(type)) {
      return DataTypeUtil.toDouble(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_STRING.equals(type)) {
      return DataTypeUtil.toLocaleNeutralString(value);
    } else if (DesignChoiceConstants.PARAM_TYPE_INTEGER.equals(type)) {
      return DataTypeUtil.toInteger(value);
    }
    return value;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Message generateMessage(Long objectId, String model, String tag, Template template)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException {

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
                I18n.get(
                    com.axelor.apps.message.exception.IExceptionMessage
                        .INVALID_MODEL_TEMPLATE_EMAIL),
                modelName,
                model));
      }
      initMaker(objectId, model, tag, template.getIsJson());
      computeTemplateContexts(
          template.getTemplateContextList(), objectId, model, template.getIsJson());
    }

    log.debug("model : {}", model);
    log.debug("tag : {}", tag);
    log.debug("object id : {}", objectId);
    log.debug("template : {}", template);

    String content = "";
    String subject = "";
    String from = "";
    String replyToRecipients = "";
    String toRecipients = "";
    String ccRecipients = "";
    String bccRecipients = "";
    String addressBlock = "";
    int mediaTypeSelect;
    String signature = "";

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

    if (template.getSignature() != null) {
      maker.setTemplate(template.getSignature());
      signature = maker.make();
      log.debug("Signature ::: {}", signature);
    }

    Message message =
        messageServiceBase.createMessage(
            model,
            Math.toIntExact(objectId),
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
            getMailAccount(),
            signature);

    message.setTemplate(Beans.get(TemplateRepository.class).find(template.getId()));

    message = Beans.get(MessageRepository.class).save(message);

    messageService.attachMetaFiles(message, getMetaFiles(template));

    return message;
  }
}
