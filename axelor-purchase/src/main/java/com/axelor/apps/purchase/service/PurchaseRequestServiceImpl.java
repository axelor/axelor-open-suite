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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.PurchaseRequestLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PurchaseRequestServiceImpl implements PurchaseRequestService {

  protected PurchaseOrderService purchaseOrderService;
  protected PurchaseOrderCreateService purchaseOrderCreateService;
  protected PurchaseOrderLineService purchaseOrderLineService;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected AppBaseService appBaseService;
  protected PurchaseRequestWorkflowService purchaseRequestWorkflowService;

  @Inject
  public PurchaseRequestServiceImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateService purchaseOrderCreateService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      AppBaseService appBaseService,
      PurchaseRequestWorkflowService purchaseRequestWorkflowService) {
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderCreateService = purchaseOrderCreateService;
    this.purchaseOrderLineService = purchaseOrderLineService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.appBaseService = appBaseService;
    this.purchaseRequestWorkflowService = purchaseRequestWorkflowService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public List<PurchaseOrder> generatePo(
      List<PurchaseRequest> purchaseRequests, Boolean groupBySupplier, Boolean groupByProduct)
      throws AxelorException {

    Map<String, PurchaseOrder> purchaseOrderMap = new HashMap<>();

    for (PurchaseRequest purchaseRequest : purchaseRequests) {
      PurchaseOrder purchaseOrder;

      String key = groupBySupplier ? getPurchaseOrderGroupBySupplierKey(purchaseRequest) : null;
      if (key != null && purchaseOrderMap.containsKey(key)) {
        purchaseOrder = purchaseOrderMap.get(key);
      } else {
        purchaseOrder = createPurchaseOrder(purchaseRequest);
        key = key == null ? purchaseRequest.getId().toString() : key;
        purchaseOrderMap.put(key, purchaseOrder);
      }

      if (purchaseOrder == null) {
        purchaseOrder = createPurchaseOrder(purchaseRequest);
      }

      this.generatePoLineListFromPurchaseRequest(purchaseRequest, purchaseOrder, groupByProduct);

      purchaseOrderService.computePurchaseOrder(purchaseOrder);
      purchaseOrderRepo.save(purchaseOrder);
    }
    List<PurchaseOrder> purchaseOrders =
        purchaseOrderMap.values().stream().collect(Collectors.toList());
    return purchaseOrders;
  }

  protected PurchaseOrder createPurchaseOrder(PurchaseRequest purchaseRequest)
      throws AxelorException {
    return purchaseOrderRepo.save(
        purchaseOrderCreateService.createPurchaseOrder(
            AuthUtils.getUser(),
            purchaseRequest.getCompany(),
            null,
            purchaseRequest.getSupplierPartner().getCurrency(),
            null,
            null,
            null,
            appBaseService.getTodayDate(purchaseRequest.getCompany()),
            null,
            purchaseRequest.getSupplierPartner(),
            null));
  }

  protected String getPurchaseOrderGroupBySupplierKey(PurchaseRequest purchaseRequest) {
    return purchaseRequest.getSupplierPartner().getId().toString();
  }

  protected void generatePoLineListFromPurchaseRequest(
      PurchaseRequest purchaseRequest, PurchaseOrder purchaseOrder, Boolean groupByProduct)
      throws AxelorException {

    for (PurchaseRequestLine purchaseRequestLine : purchaseRequest.getPurchaseRequestLineList()) {

      PurchaseOrderLine purchaseOrderLine =
          groupByProduct ? getPoLineByProductAndUnit(purchaseRequestLine, purchaseOrder) : null;
      if (purchaseOrderLine != null) {
        purchaseOrderLine.setQty(purchaseOrderLine.getQty().add(purchaseRequestLine.getQuantity()));
      } else {
        purchaseOrderLine =
            purchaseOrderLineService.createPurchaseOrderLine(
                purchaseOrder,
                purchaseRequestLine.getProduct(),
                purchaseRequestLine.getNewProduct() ? purchaseRequestLine.getProductTitle() : null,
                null,
                purchaseRequestLine.getQuantity(),
                purchaseRequestLine.getUnit());
        purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);
      }

      purchaseOrderLineService.compute(purchaseOrderLine, purchaseOrder);
    }
  }

  protected PurchaseOrderLine getPoLineByProductAndUnit(
      PurchaseRequestLine purchaseRequestLine, PurchaseOrder purchaseOrder) {

    return purchaseOrder.getPurchaseOrderLineList().stream()
        .filter(
            l ->
                l != null
                    && (!purchaseRequestLine.getNewProduct()
                        ? purchaseRequestLine.getProduct().equals(l.getProduct())
                        : purchaseRequestLine.getProductTitle().equals(l.getProductName()))
                    && (purchaseRequestLine.getUnit() == null
                        || purchaseRequestLine.getUnit().equals(l.getUnit())))
        .findFirst()
        .orElse(null);
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
