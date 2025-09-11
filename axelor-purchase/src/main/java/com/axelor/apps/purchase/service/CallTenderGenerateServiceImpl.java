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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.repo.CallTenderOfferRepository;
import com.axelor.apps.purchase.db.repo.CallTenderRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Objects;

public class CallTenderGenerateServiceImpl implements CallTenderGenerateService {

  protected final CallTenderOfferService callTenderOfferService;
  protected final CallTenderOfferRepository callTenderOfferRepository;
  protected final CallTenderRepository callTenderRepository;

  @Inject
  public CallTenderGenerateServiceImpl(
      CallTenderOfferService callTenderOfferService,
      CallTenderOfferRepository callTenderOfferRepository,
      CallTenderRepository callTenderRepository) {
    this.callTenderOfferService = callTenderOfferService;
    this.callTenderOfferRepository = callTenderOfferRepository;
    this.callTenderRepository = callTenderRepository;
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
                callTenderOfferService.setCounter(resultOffer, callTender);
                callTenderOfferRepository.save(resultOffer);
              }
            });
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public CallTender generateCallTender(
      String name, Company company, List<CallTenderNeed> callTenderNeedList) {

    Objects.requireNonNull(name);
    Objects.requireNonNull(callTenderNeedList);

    var callTender = new CallTender();

    callTender.setName(name);
    callTender.setCompany(company);
    callTenderNeedList.stream().forEach(callTender::addCallTenderNeedListItem);

    return callTenderRepository.save(callTender);
  }
}
