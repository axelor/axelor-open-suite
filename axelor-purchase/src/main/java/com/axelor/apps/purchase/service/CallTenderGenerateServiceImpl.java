package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.CallTender;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Objects;

public class CallTenderGenerateServiceImpl implements CallTenderGenerateService {

  protected final CallTenderOfferService callTenderOfferService;

  @Inject
  public CallTenderGenerateServiceImpl(CallTenderOfferService callTenderOfferService) {
    this.callTenderOfferService = callTenderOfferService;
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
}
