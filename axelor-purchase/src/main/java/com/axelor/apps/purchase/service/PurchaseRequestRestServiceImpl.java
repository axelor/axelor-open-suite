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
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.rest.dto.PurchaseRequestLineRequest;
import com.axelor.apps.purchase.rest.dto.PurchaseRequestPostRequest;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PurchaseRequestRestServiceImpl implements PurchaseRequestRestService {

  protected PurchaseRequestService purchaseRequestService;
  protected PurchaseRequestLineService purchaseRequestLineService;
  protected PurchaseRequestRepository purchaseRequestRepository;

  @Inject
  public PurchaseRequestRestServiceImpl(
      PurchaseRequestService purchaseRequestService,
      PurchaseRequestLineService purchaseRequestLineService,
      PurchaseRequestRepository purchaseRequestRepository) {
    this.purchaseRequestService = purchaseRequestService;
    this.purchaseRequestLineService = purchaseRequestLineService;
    this.purchaseRequestRepository = purchaseRequestRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public PurchaseRequest createPurchaseRequest(PurchaseRequestPostRequest requestBody)
      throws AxelorException {
    PurchaseRequest purchaseRequest =
        purchaseRequestService.createPurchaseRequest(
            requestBody.fetchCompany(), requestBody.getStatus(), requestBody.getDescription());
    createPurchaseRequestLines(requestBody.getPurchaseRequestLineList(), purchaseRequest);
    return purchaseRequestRepository.save(purchaseRequest);
  }

  protected void createPurchaseRequestLines(
      List<PurchaseRequestLineRequest> purchaseRequestLineList, PurchaseRequest purchaseRequest) {
    if (CollectionUtils.isEmpty(purchaseRequestLineList)) {
      return;
    }
    for (PurchaseRequestLineRequest purchaseRequestLine : purchaseRequestLineList) {
      purchaseRequestLineService.createPurchaseRequestLine(
          purchaseRequest,
          purchaseRequestLine.fetchProduct(),
          purchaseRequestLine.getProductTitle(),
          purchaseRequestLine.fetchUnit(),
          purchaseRequestLine.getQuantity());
    }
  }
}
