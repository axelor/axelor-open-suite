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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.service.purchase.request.PurchaseRequestToPoCreateService;
import com.axelor.apps.purchase.service.purchase.request.PurchaseRequestToPoGenerationResult;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PurchaseRequestServiceImpl implements PurchaseRequestService {

  protected final PurchaseRequestWorkflowService purchaseRequestWorkflowService;
  protected final PurchaseRequestRepository purchaseRequestRepository;
  protected final PurchaseRequestToPoCreateService purchaseRequestToPoCreateService;

  @Inject
  public PurchaseRequestServiceImpl(
      PurchaseRequestWorkflowService purchaseRequestWorkflowService,
      PurchaseRequestRepository purchaseRequestRepository,
      PurchaseRequestToPoCreateService purchaseRequestToPoCreateService) {
    this.purchaseRequestWorkflowService = purchaseRequestWorkflowService;
    this.purchaseRequestRepository = purchaseRequestRepository;
    this.purchaseRequestToPoCreateService = purchaseRequestToPoCreateService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseRequestToPoGenerationResult generatePo(
      List<PurchaseRequest> purchaseRequests,
      Boolean groupBySupplier,
      Boolean groupByProduct,
      Company company)
      throws AxelorException {
    PurchaseRequestToPoGenerationResult result =
        purchaseRequestToPoCreateService.createFromRequests(
            purchaseRequests, groupBySupplier, groupByProduct, company);

    for (PurchaseRequest pr : purchaseRequests) {
      if (pr.getPurchaseOrder() != null) {
        pr.setStatusSelect(PurchaseRequestRepository.STATUS_PURCHASED);
      }
    }
    return result;
  }

  @Override
  public Map<String, Object> getDefaultValues(PurchaseRequest purchaseRequest, Company company)
      throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    if (company == null) {
      company = Optional.of(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    }
    purchaseRequest.setCompany(company);
    values.put("company", purchaseRequest.getCompany());
    return values;
  }

  @Override
  public PurchaseRequest createPurchaseRequest(Company company, Integer status, String description)
      throws AxelorException {
    PurchaseRequest purchaseRequest = new PurchaseRequest();
    getDefaultValues(purchaseRequest, company);
    setStatus(purchaseRequest, status);
    purchaseRequest.setDescription(description);
    return purchaseRequest;
  }

  protected void setStatus(PurchaseRequest purchaseRequest, Integer status) throws AxelorException {
    if (status != null && status == PurchaseRequestRepository.STATUS_REQUESTED) {
      purchaseRequestWorkflowService.requestPurchaseRequest(purchaseRequest);
    } else {
      purchaseRequest.setStatusSelect(PurchaseRequestRepository.STATUS_DRAFT);
    }
  }
}
