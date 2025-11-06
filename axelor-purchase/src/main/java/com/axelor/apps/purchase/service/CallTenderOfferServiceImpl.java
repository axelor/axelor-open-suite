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

import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.CallTenderOffer;
import com.axelor.apps.purchase.db.CallTenderSupplier;
import com.axelor.apps.purchase.db.repo.CallTenderOfferRepository;
import com.axelor.common.ObjectUtils;
import java.util.Collections;
import java.util.Comparator;
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
              return createCallTenderOffer(supplier, need);
            })
        .collect(Collectors.toList());
  }

  @Override
  public CallTenderOffer createCallTenderOffer(CallTenderSupplier supplier, CallTenderNeed need) {
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

  @Override
  public void setCounter(CallTenderOffer offer, CallTender callTender) {
    if (callTender == null) {
      return;
    }
    int counter =
        ObjectUtils.notEmpty(callTender.getCallTenderOfferList())
            ? callTender.getCallTenderOfferList().stream()
                .map(CallTenderOffer::getCounter)
                .max(Comparator.naturalOrder())
                .orElse(0)
            : 0;
    offer.setCounter(counter + 1);
  }
}
