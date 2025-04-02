package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.CallTenderSupplier;
import com.axelor.apps.purchase.db.repo.CallTenderOfferRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CallTenderOfferServiceImpl implements CallTenderOfferService {

  @Override
  public List<CallTenderOffer> generateCallTenderOfferList(
      CallTenderSupplier supplier, List<CallTenderNeed> needs) {
    Objects.requireNonNull(supplier);

    if (needs == null) {
      return Collections.emptyList();
    }

    return needs.stream()
        .map(
            need -> {
              CallTenderOffer callTenderOffer = new CallTenderOffer();
              callTenderOffer.setCallTenderSupplier(supplier);
              callTenderOffer.setProduct(need.getProduct());
              callTenderOffer.setCallTenderNeed(need);
              callTenderOffer.setRequestedQty(need.getRequestedQty());
              callTenderOffer.setRequestedDate(need.getRequestedDate());
              callTenderOffer.setRequestedUnit(need.getUnit());
              callTenderOffer.setStatusSelect(CallTenderOfferRepository.STATUS_DRAFT);
              callTenderOffer.setSupplierPartner(supplier.getSupplierPartner());
              return callTenderOffer;
            })
        .collect(Collectors.toList());
  }

  @Override
  public boolean alreadyGenerated(CallTenderOffer offer, List<CallTenderOffer> offerList) {
    Objects.requireNonNull(offer);

    if (offerList == null) {
      return false;
    }
    return offerList.stream()
        .anyMatch(
            offerInList ->
                offer.getCallTenderNeed().equals(offerInList.getCallTenderNeed())
                    && offer.getCallTenderSupplier().equals(offerInList.getCallTenderSupplier()));
  }
}
