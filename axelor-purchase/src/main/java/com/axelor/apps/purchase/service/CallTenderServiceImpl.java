package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.CallTenderSupplier;
import com.axelor.apps.purchase.db.repo.CallTenderOfferRepository;
import com.google.inject.Inject;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CallTenderServiceImpl implements CallTenderService {

    protected final CallTenderOfferService callTenderOfferService;

    @Inject
    public CallTenderServiceImpl(CallTenderOfferService callTenderOfferService) {
        this.callTenderOfferService = callTenderOfferService;
    }

    @Override
  public void generateCallTenderOffers(CallTender callTender) {
    Objects.requireNonNull(callTender);

    callTender.getCallTenderSupplierList().stream()
        .map(supplier -> callTenderOfferService.generateCallTenderOfferList(supplier, callTender.getCallTenderNeedList()))
        .flatMap(List::stream)
        .forEach(resultOffer -> {
        if (!callTenderOfferService.alreadyGenerated(resultOffer, callTender.getCallTenderOfferList())) {
            callTender.addCallTenderOfferListItem(resultOffer);
        }
    });

  }


}
