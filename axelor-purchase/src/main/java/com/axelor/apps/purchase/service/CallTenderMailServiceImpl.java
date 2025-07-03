/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.mail.MessagingException;

public class CallTenderMailServiceImpl implements CallTenderMailService {

  protected final MessageService messageService;
  protected final TemplateMessageService templateMessageService;
  protected final CallTenderCsvService callTenderCsvService;

  @Inject
  public CallTenderMailServiceImpl(
      MessageService messageService,
      TemplateMessageService templateMessageService,
      CallTenderCsvService callTenderCsvService) {
    this.messageService = messageService;
    this.templateMessageService = templateMessageService;
    this.callTenderCsvService = callTenderCsvService;
  }

  @Override
  public void sendMails(CallTender callTender) throws ClassNotFoundException, MessagingException {

    var offersGroupBySupplier =
        callTender.getCallTenderOfferList().stream()
            .filter(offer -> offer.getStatusSelect().equals(CallTenderOfferRepository.STATUS_DRAFT))
            .collect(Collectors.groupingBy(CallTenderOffer::getSupplierPartner));

    for (List<CallTenderOffer> offerList : offersGroupBySupplier.values()) {
      // Offer list should not be empty there
      // We will pick one randomly as it should be the same mail
      var anyOffer = offerList.get(0);
      CallTenderMail offerMail = anyOffer.getOfferMail();
      var messageToSend =
          templateMessageService.generateMessage(anyOffer, offerMail.getMailTemplate());
      // Add all contacts to email address
      var contacts =
          getContactPartnerList(
              anyOffer.getSupplierPartner(), callTender.getCallTenderSupplierList());
      contacts.stream()
          .map(Partner::getEmailAddress)
          .filter(Objects::nonNull)
          .forEach(messageToSend::addToEmailAddressSetItem);
      messageService.attachMetaFiles(messageToSend, Set.of(offerMail.getMetaFile()));
      var sentMessage = messageService.sendByEmail(messageToSend);
      offerMail.setSentMessage(sentMessage);
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

    // Generate csv here
    var csv = callTenderCsvService.generateCsvFile(offerList);
    callTenderMail.setMetaFile(csv);

    for (CallTenderOffer offer : offerList) {
      offer.setOfferMail(callTenderMail);
    }
  }

  @Override
  public void sendCallTenderOffers(CallTender callTender)
      throws AxelorException, IOException, MessagingException, ClassNotFoundException {
    Objects.requireNonNull(callTender);

    if (callTender.getCallTenderOfferList() == null) {
      return;
    }

    // Get template
    var template = callTender.getCallForTenderMailTemplate();

    // Generate callTenderMail
    var offerToGenerateMailGroupBySupplier =
        callTender.getCallTenderOfferList().stream()
            .filter(offer -> offer.getOfferMail() == null)
            .collect(Collectors.groupingBy(CallTenderOffer::getSupplierPartner));

    if (offerToGenerateMailGroupBySupplier.isEmpty()) {
      return;
    }

    for (List<CallTenderOffer> offerList : offerToGenerateMailGroupBySupplier.values()) {
      generateOfferMail(offerList, template);
    }

    sendMails(callTender);
    updateOffersStatus(callTender);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void updateOffersStatus(CallTender callTender) {
    callTender.getCallTenderOfferList().stream()
        .filter(offer -> offer.getOfferMail().getSentMessage() != null)
        .forEach(offer -> offer.setStatusSelect(CallTenderOfferRepository.STATUS_SENT));
  }
}
