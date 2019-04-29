/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseRequestServiceImpl implements PurchaseRequestService {

  @Inject private PurchaseRequestRepository purchaseRequestRepo;
  @Inject private PurchaseOrderService purchaseOrderService;
  @Inject private PurchaseOrderLineService purchaseOrderLineService;
  @Inject private PurchaseOrderRepository purchaseOrderRepo;
  @Inject private PurchaseOrderLineRepository purchaseOrderLineRepo;

  @Transactional
  @Override
  public void confirmCart() {

    List<PurchaseRequest> purchaseRequests =
        purchaseRequestRepo
            .all()
            .filter("self.statusSelect = 1 and self.createdBy = ?1", AuthUtils.getUser())
            .fetch();

    for (PurchaseRequest purchaseRequest : purchaseRequests) {
      purchaseRequest.setStatusSelect(2);
      purchaseRequestRepo.save(purchaseRequest);
    }
  }

  @Transactional
  @Override
  public void acceptRequest(List<PurchaseRequest> purchaseRequests) {

    for (PurchaseRequest purchaseRequest : purchaseRequests) {
      purchaseRequest.setStatusSelect(3);
      purchaseRequestRepo.save(purchaseRequest);
    }
  }

  @Transactional
  @Override
  public List<PurchaseOrder> generatePo(
      List<PurchaseRequest> purchaseRequests,
      Boolean groupBySupplier,
      Boolean groupByProduct,
      Boolean groupByDeliveryAddress)
      throws AxelorException {
    List<PurchaseOrder> purchaseOrderList = new ArrayList<>();

    for (PurchaseRequest purchaseRequest : purchaseRequests) {

      PurchaseOrder purchaseOrder =
          groupBySupplier && groupByDeliveryAddress
              ? this.getPoBySupplierAndDeliveryAddress(purchaseRequest, purchaseOrderList)
              : null;
      Product product = purchaseRequest.getProduct();
      PurchaseOrderLine purchaseOrderLine =
          groupByProduct && purchaseOrder != null
              ? this.getPoLineByProduct(product, purchaseOrder)
              : null;

      if (purchaseOrder == null) {
        purchaseOrder = createPurchaseOrder(purchaseRequest);
      }
      if (purchaseOrderLine == null) {
        purchaseOrderLine =
            purchaseOrderLineService.createPurchaseOrderLine(
                purchaseOrder,
                product,
                product.getName(),
                product.getDescription(),
                purchaseRequest.getQuantity(),
                purchaseRequest.getUnit());
        purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);
      } else {
        purchaseOrderLine.setQty(purchaseOrderLine.getQty().add(purchaseRequest.getQuantity()));
      }
      purchaseOrderLineService.compute(purchaseOrderLine, purchaseOrder);
      purchaseOrderService.computePurchaseOrder(purchaseOrder);
      purchaseOrderLineRepo.save(purchaseOrderLine);
      purchaseOrderList.add(purchaseOrder);
    }

    return purchaseOrderList;
  }

  protected PurchaseOrder getPoBySupplierAndDeliveryAddress(
      PurchaseRequest purchaseRequest, List<PurchaseOrder> purchaseOrderList) {

    PurchaseOrder purchaseOrder =
        purchaseOrderList != null && !purchaseOrderList.isEmpty()
            ? purchaseOrderList
                .stream()
                .filter(po -> po.getSupplierPartner().equals(purchaseRequest.getSupplier()))
                .findFirst()
                .orElse(null)
            : null;

    return purchaseOrder;
  }

  private PurchaseOrderLine getPoLineByProduct(Product product, PurchaseOrder purchaseOrder) {

    PurchaseOrderLine purchaseOrderLine =
        purchaseOrder.getPurchaseOrderLineList() != null
                && !purchaseOrder.getPurchaseOrderLineList().isEmpty()
            ? purchaseOrder
                .getPurchaseOrderLineList()
                .stream()
                .filter(poLine -> poLine.getProduct().equals(product))
                .findFirst()
                .orElse(null)
            : null;
    return purchaseOrderLine;
  }

  protected PurchaseOrder createPurchaseOrder(PurchaseRequest purchaseRequest)
      throws AxelorException {
    return purchaseOrderRepo.save(
        purchaseOrderService.createPurchaseOrder(
            AuthUtils.getUser(),
            purchaseRequest.getCompany(),
            null,
            purchaseRequest.getSupplier().getCurrency(),
            null,
            null,
            null,
            LocalDate.now(),
            null,
            purchaseRequest.getSupplier(),
            null));
  }
}
