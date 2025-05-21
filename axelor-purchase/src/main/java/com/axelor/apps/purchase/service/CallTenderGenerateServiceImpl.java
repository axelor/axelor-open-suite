package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.repo.CallTenderOfferRepository;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.mail.MessagingException;

public class CallTenderGenerateServiceImpl implements CallTenderGenerateService {

  protected final CallTenderOfferService callTenderOfferService;
  protected final CallTenderMailService callTenderMailService;

  @Inject
  public CallTenderGenerateServiceImpl(
      CallTenderOfferService callTenderOfferService,
      TemplateMessageService templateMessageService,
      MessageService messageService,
      CallTenderMailService callTenderMailService) {
    this.callTenderOfferService = callTenderOfferService;
    this.callTenderMailService = callTenderMailService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generateCallTenderOffers(CallTender callTender) {
    Objects.requireNonNull(callTender);

    if (callTender.getCallTenderSupplierList() == null) {
      return;
    }

    callTender.getCallTenderSupplierList().stream()
        .map(
            supplier ->
                callTenderOfferService.generateCallTenderOfferList(
                    supplier, callTender.getCallTenderNeedList()))
        .flatMap(List::stream)
        .forEach(
            resultOffer -> {
              if (!callTenderOfferService.alreadyGenerated(
                  resultOffer, callTender.getCallTenderOfferList())) {
                callTender.addCallTenderOfferListItem(resultOffer);
              }
            });
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
      callTenderMailService.generateOfferMail(offerList, template);
    }

    callTenderMailService.sendMails(callTender);
    updateOffersStatus(callTender);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void updateOffersStatus(CallTender callTender) {
    callTender.getCallTenderOfferList().stream()
        .filter(offer -> offer.getOfferMail().getSentMessage() != null)
        .forEach(offer -> offer.setStatusSelect(CallTenderOfferRepository.STATUS_SENT));
  }
}
