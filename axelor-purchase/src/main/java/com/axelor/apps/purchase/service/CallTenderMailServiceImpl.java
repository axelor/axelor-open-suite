/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderMail;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.CallTenderSupplier;
import com.axelor.apps.purchase.db.repo.CallTenderOfferRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CallTenderMailServiceImpl implements CallTenderMailService {

  protected final MessageService messageService;
  protected final TemplateMessageService templateMessageService;
  protected final CallTenderExcelService callTenderExcelService;

  @Inject
  public CallTenderMailServiceImpl(
      MessageService messageService,
      TemplateMessageService templateMessageService,
      CallTenderExcelService callTenderExcelService) {
    this.messageService = messageService;
    this.templateMessageService = templateMessageService;
    this.callTenderExcelService = callTenderExcelService;
  }

  @Override
  public void sendMails(CallTender callTender) throws ClassNotFoundException, MessagingException {

    var offersGroupBySupplier =
        callTender.getCallTenderOfferList().stream()
            .filter(
                offer ->
                    offer.getOfferMail() != null
                        && offer.getOfferMail().getEmailMessage() != null
                        && offer.getStatusSelect().equals(CallTenderOfferRepository.STATUS_DRAFT))
            .collect(Collectors.groupingBy(CallTenderOffer::getSupplierPartner));

    for (List<CallTenderOffer> offerList : offersGroupBySupplier.values()) {
      var anyOffer = offerList.get(0);
      CallTenderMail offerMail = anyOffer.getOfferMail();
      messageService.sendByEmail(offerMail.getEmailMessage());
    }
  }

  protected List<Partner> getContactPartnerList(
      Partner partner, List<CallTenderSupplier> suppliers) {
    if (suppliers != null) {
      return suppliers.stream()
          .filter(supplier -> supplier.getSupplierPartner().equals(partner))
          .limit(1)
          .map(CallTenderSupplier::getContactPartnerSet)
          .flatMap(Set::stream)
          .collect(Collectors.toList());
    }
    return List.of();
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void generateOfferMail(List<CallTenderOffer> offerList, Template template)
      throws AxelorException, IOException {
    Objects.requireNonNull(offerList);

    if (offerList.isEmpty()) {
      return;
    }

    if (template == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(PurchaseExceptionMessage.CALL_FOR_TENDER_MISSING_TEMPLATE));
    }

    var callTenderMail = new CallTenderMail();
    callTenderMail.setMailTemplate(template);

    // Generate excel and attach only if attachFileEmail is checked
    if (Boolean.TRUE.equals(offerList.get(0).getCallTender().getAttachFileEmail())) {
      var excelFile = callTenderExcelService.generateExcelFile(offerList);
      callTenderMail.setMetaFile(excelFile);
    }

    for (CallTenderOffer offer : offerList) {
      offer.setOfferMail(callTenderMail);
    }
  }

  protected Message generateEmailMessage(
      CallTenderOffer anyOffer, CallTender callTender, CallTenderMail offerMail)
      throws ClassNotFoundException, MessagingException {

    Message emailMessage =
        templateMessageService.generateMessage(anyOffer, offerMail.getMailTemplate());

    var contacts =
        getContactPartnerList(
            anyOffer.getSupplierPartner(), callTender.getCallTenderSupplierList());
    contacts.stream()
        .map(Partner::getEmailAddress)
        .filter(Objects::nonNull)
        .forEach(emailMessage::addToEmailAddressSetItem);

    if (offerMail.getMetaFile() != null) {
      messageService.attachMetaFiles(emailMessage, Set.of(offerMail.getMetaFile()));
    }

    return emailMessage;
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void generateCallTenderEmails(CallTender callTender)
      throws AxelorException, IOException, ClassNotFoundException, MessagingException {
    Objects.requireNonNull(callTender);

    if (callTender.getCallTenderOfferList() == null) {
      return;
    }

    var template = callTender.getCallForTenderMailTemplate();

    var offerToGenerateMailGroupBySupplier =
        callTender.getCallTenderOfferList().stream()
            .filter(offer -> offer.getOfferMail() == null)
            .collect(Collectors.groupingBy(CallTenderOffer::getSupplierPartner));

    if (offerToGenerateMailGroupBySupplier.isEmpty()) {
      return;
    }

    for (List<CallTenderOffer> offerList : offerToGenerateMailGroupBySupplier.values()) {
      generateOfferMail(offerList, template);

      var anyOffer = offerList.get(0);
      CallTenderMail offerMail = anyOffer.getOfferMail();
      Message emailMessage = generateEmailMessage(anyOffer, callTender, offerMail);
      offerMail.setEmailMessage(emailMessage);
    }
  }

  @Override
  public void sendCallTenderOffers(CallTender callTender)
      throws ClassNotFoundException, MessagingException {
    Objects.requireNonNull(callTender);

    if (callTender.getCallTenderOfferList() == null) {
      return;
    }

    sendMails(callTender);
    updateOffersStatus(callTender);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void updateOffersStatus(CallTender callTender) {
    callTender.getCallTenderOfferList().stream()
        .filter(
            offer -> offer.getOfferMail() != null && offer.getOfferMail().getEmailMessage() != null)
        .forEach(offer -> offer.setStatusSelect(CallTenderOfferRepository.STATUS_SENT));
  }
}
