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
package com.axelor.apps.portal.service;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.AppPortal;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.repo.AppPortalRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.client.portal.db.PortalQuotation;
import com.axelor.apps.client.portal.db.repo.PortalQuotationRepository;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.TemplateContext;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.portal.translation.ITranslation;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.EntityHelper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaAttachment;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaAttachmentRepository;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import javax.mail.MessagingException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalQuotationServiceImpl implements PortalQuotationService {

  @Inject protected MessageRepository messageRepository;
  @Inject protected EmailAccountRepository emailAccountRepo;
  @Inject protected PortalQuotationRepository portalQuotationRepo;

  @Inject protected MetaFiles metaFiles;
  @Inject protected UserService userService;
  @Inject protected MessageService messageService;
  @Inject protected SaleOrderWorkflowService saleOrderWorkflowService;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  @Transactional
  public PortalQuotation createPortalQuotation(SaleOrder saleOrder)
      throws MessagingException, AxelorException {

    PortalQuotation portalQuotation = new PortalQuotation();
    portalQuotation.setSaleOrder(saleOrder);
    portalQuotation.setExTaxTotal(saleOrder.getExTaxTotal());
    portalQuotation.setStatusSelect(SaleOrderRepository.STATUS_DRAFT_QUOTATION);
    portalQuotationRepo.save(portalQuotation);

    List<MetaAttachment> attachments =
        Beans.get(MetaAttachmentRepository.class)
            .all()
            .filter(
                "self.objectId = ? AND self.objectName = ?",
                saleOrder.getId(),
                EntityHelper.getEntityClass(saleOrder).getName())
            .fetch();
    for (MetaAttachment metaAttachment : attachments) {
      MetaFile file = metaAttachment.getMetaFile();
      metaFiles.attach(file, file.getFileName(), portalQuotation);
    }

    generateReport(saleOrder, portalQuotation);
    sendQuotationCreationMessage(saleOrder, portalQuotation);
    return portalQuotation;
  }

  protected void generateReport(SaleOrder saleOrder, PortalQuotation portalQuotation) {

    try {
      String title = Beans.get(SaleOrderService.class).getFileName(saleOrder) + ".pdf";
      File file =
          Beans.get(SaleOrderPrintService.class).print(saleOrder, false, ReportSettings.FORMAT_PDF);
      MetaFile metaFile = Beans.get(MetaFiles.class).upload(FileUtils.openInputStream(file), title);
      metaFile.setSharedWith(saleOrder.getClientPartner());
      portalQuotation.setReport(metaFile);
    } catch (AxelorException | IOException e) {
      TraceBackService.trace(e);
    }
  }

  protected void sendQuotationCreationMessage(
      SaleOrder saleOrder, PortalQuotation portalQuotation) {

    try {
      Message message;
      String subject = I18n.get(ITranslation.PORTAL_QUATATION_GENERATION);
      String link =
          String.format(
              "\"<a href='%s/#/ds/client.portal.portal.quatation/edit/%s'>%s</a>\"",
              AppSettings.get().getBaseURL(), portalQuotation.getId(), saleOrder.getSaleOrderSeq());

      AppPortal appPortal = Beans.get(AppPortalRepository.class).all().fetchOne();
      Template template = appPortal.getQuotationGenerationTemplate();
      if (template != null) {
        setContextValue(template, "QGquotationLink", link);
        message =
            Beans.get(TemplateMessageService.class).generateMessage(portalQuotation, template);
        setContextValue(template, "QGquotationLink", null);

      } else {
        String content = String.format("%s : %s", subject, link);
        List<EmailAddress> toEmailAddresses = new ArrayList<>();
        if (saleOrder.getContactPartner() != null
            && saleOrder.getContactPartner().getEmailAddress() != null) {
          toEmailAddresses.add(saleOrder.getContactPartner().getEmailAddress());
        } else if (saleOrder.getClientPartner().getEmailAddress() != null) {
          toEmailAddresses.add(saleOrder.getClientPartner().getEmailAddress());
        }

        message =
            messageService.createMessage(
                PortalQuotation.class.getCanonicalName(),
                portalQuotation.getId(),
                subject,
                content,
                null,
                null,
                toEmailAddresses,
                null,
                null,
                null,
                null,
                MessageRepository.MEDIA_TYPE_EMAIL,
                emailAccountRepo.all().filter("self.isDefault = true").fetchOne(),
                null);
      }

      messageService.sendByEmail(message);

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  @Override
  @Transactional
  public Integer sendConfirmCode(PortalQuotation portalQuotation)
      throws MessagingException, AxelorException {

    Integer randomCode = new Random().nextInt(9999999);
    log.info(String.format("Confirmation Code : %s", randomCode));
    String subject = I18n.get(ITranslation.PORTAL_QUATATION_CONFIRM_CODE);
    String content = String.format("%s : %s", subject, randomCode);
    User user = userService.getUser();
    EmailAddress toEmailAddress = null;
    if (user.getPartner() != null
        && user.getPartner().getEmailAddress() != null
        && StringUtils.notBlank(user.getPartner().getEmailAddress().getAddress())) {
      toEmailAddress = user.getPartner().getEmailAddress();
    }

    try {
      Message message;
      AppPortal appPortal = Beans.get(AppPortalRepository.class).all().fetchOne();
      Template template = appPortal.getQuotationConfimationCodeTemplate();
      if (template != null) {
        setContextValue(template, "confirmationCode", String.format("\"%s\"", randomCode));
        message =
            Beans.get(TemplateMessageService.class).generateMessage(portalQuotation, template);
        message.addToEmailAddressSetItem(toEmailAddress);
        setContextValue(template, "confirmationCode", null);

      } else {

        List<EmailAddress> toEmailAddresses = new ArrayList<>();
        toEmailAddresses.add(toEmailAddress);
        message =
            messageService.createMessage(
                PortalQuotation.class.getCanonicalName(),
                portalQuotation.getId(),
                subject,
                content,
                null,
                null,
                toEmailAddresses,
                null,
                null,
                null,
                null,
                MessageRepository.MEDIA_TYPE_EMAIL,
                emailAccountRepo.all().filter("self.isDefault = true").fetchOne(),
                null);
      }

      messageService.sendByEmail(message);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return randomCode;
  }

  @Override
  @Transactional
  public void confirmPortalQuotation(PortalQuotation portalQuotation, String name)
      throws MessagingException, AxelorException {

    SaleOrder saleOrder = portalQuotation.getSaleOrder();
    if (saleOrder == null) {
      return;
    }

    saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
    boolean isFinalised = true;
    if (SaleOrderRepository.STATUS_DRAFT_QUOTATION == saleOrder.getStatusSelect()) {
      saleOrderWorkflowService.finalizeQuotation(saleOrder);
      isFinalised = false;
    }
    saleOrderWorkflowService.confirmSaleOrder(saleOrder);
    portalQuotation = portalQuotationRepo.find(portalQuotation.getId());
    portalQuotation.setStatusSelect(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    portalQuotationRepo.save(portalQuotation);
    sendQuotationConfirmationMessage(portalQuotation, saleOrder, name, isFinalised);
  }

  protected void sendQuotationConfirmationMessage(
      PortalQuotation portalQuotation, SaleOrder saleOrder, String name, boolean isFinalised) {

    try {
      Message message;
      AppPortal appPortal = Beans.get(AppPortalRepository.class).all().fetchOne();
      Template template = appPortal.getQuotationConfimationTemplate();
      if (template != null) {
        String link =
            String.format(
                "\"<a href='%s/#/ds/client.portal.portal.quatation/edit/%s'>%s</a>\"",
                AppSettings.get().getBaseURL(),
                portalQuotation.getId(),
                saleOrder.getSaleOrderSeq());

        Set<BirtTemplate> reports = template.getBirtTemplateSet();
        if (isFinalised) {
          template.setBirtTemplateSet(null);
        }
        setContextValue(template, "QCquotationLink", link);
        setContextValue(template, "signatureName", String.format("\"%s\"", name));
        message =
            Beans.get(TemplateMessageService.class).generateMessage(portalQuotation, template);
        setContextValue(template, "QCquotationLink", null);
        setContextValue(template, "signatureName", null);
        if (isFinalised && ObjectUtils.notEmpty(reports)) {
          template.setBirtTemplateSet(reports);
        }
      } else {
        List<EmailAddress> toEmailAddresses = new ArrayList<>();
        if (saleOrder.getContactPartner() != null
            && saleOrder.getContactPartner().getEmailAddress() != null) {
          toEmailAddresses.add(saleOrder.getContactPartner().getEmailAddress());
        } else if (saleOrder.getClientPartner().getEmailAddress() != null) {
          toEmailAddresses.add(saleOrder.getClientPartner().getEmailAddress());
        }

        List<EmailAddress> ccEmailAddresses = new ArrayList<>();
        if (saleOrder.getSalespersonUser() != null
            && saleOrder.getSalespersonUser().getPartner() != null
            && saleOrder.getSalespersonUser().getPartner().getEmailAddress() != null) {
          ccEmailAddresses.add(saleOrder.getSalespersonUser().getPartner().getEmailAddress());
        }
        if (saleOrder.getCompany().getEmailAddress() != null) {
          ccEmailAddresses.add(saleOrder.getCompany().getEmailAddress());
        }

        String subject = I18n.get(ITranslation.PORTAL_QUATATION_CONFIRMATION);
        message =
            messageService.createMessage(
                PortalQuotation.class.getCanonicalName(),
                portalQuotation.getId(),
                subject,
                subject,
                null,
                null,
                toEmailAddresses,
                ccEmailAddresses,
                null,
                null,
                null,
                MessageRepository.MEDIA_TYPE_EMAIL,
                emailAccountRepo.all().filter("self.isDefault = true").fetchOne(),
                null);
      }

      Set<MetaFile> metaFiles = new HashSet<>();
      if (portalQuotation.getSignature() != null) {
        metaFiles.add(portalQuotation.getSignature());
      }
      List<MetaFile> attachments =
          Beans.get(MetaFileRepository.class)
              .all()
              .filter(
                  "self IN (SELECT metaFile FROM MetaAttachment metaAttach WHERE metaAttach.objectId = :objectId AND metaAttach.objectName = :objectName)")
              .bind("objectId", portalQuotation.getId())
              .bind("objectName", EntityHelper.getEntityClass(portalQuotation).getName())
              .fetch();
      metaFiles.addAll(attachments);
      messageService.attachMetaFiles(message, metaFiles);

      messageService.sendByEmail(message);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  protected void setContextValue(Template template, String key, String value) {

    Optional<TemplateContext> templateContextOpt =
        template.getTemplateContextList().stream()
            .filter(templateContext -> templateContext.getName().equals(key))
            .findFirst();
    if (templateContextOpt.isPresent()) {
      templateContextOpt.get().setValue(value);
    }
  }
}
