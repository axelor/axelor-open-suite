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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.PurchaseRequestServiceImpl;
import com.axelor.apps.purchase.service.PurchaseRequestWorkflowService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Map;

public class PurchaseRequestServiceSupplychainImpl extends PurchaseRequestServiceImpl {

  protected AppSupplychainService appSupplychainService;
  protected PurchaseOrderSupplychainService purchaseOrderSupplychainService;

  @Inject
  public PurchaseRequestServiceSupplychainImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateService purchaseOrderCreateService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      AppBaseService appBaseService,
      PurchaseRequestWorkflowService purchaseRequestWorkflowService,
      AppSupplychainService appSupplychainService,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService) {
    super(
        purchaseOrderService,
        purchaseOrderCreateService,
        purchaseOrderLineService,
        purchaseOrderRepo,
        appBaseService,
        purchaseRequestWorkflowService);
    this.appSupplychainService = appSupplychainService;
    this.purchaseOrderSupplychainService = purchaseOrderSupplychainService;
  }

  @Override
  protected PurchaseOrder createPurchaseOrder(PurchaseRequest purchaseRequest)
      throws AxelorException {

    PurchaseOrder purchaseOrder = super.createPurchaseOrder(purchaseRequest);

    if (Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      purchaseOrder.setStockLocation(purchaseRequest.getStockLocation());
    }
    return purchaseOrder;
  }

  @Override
  protected String getPurchaseOrderGroupBySupplierKey(PurchaseRequest purchaseRequest) {
    String key = super.getPurchaseOrderGroupBySupplierKey(purchaseRequest);

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return key;
    }

    StockLocation stockLocation = purchaseRequest.getStockLocation();
    if (stockLocation != null) {
      key = key + "_" + stockLocation.getId().toString();
    }
    return key;
  }

  @Override
  public Map<String, Object> getDefaultValues(PurchaseRequest purchaseRequest, Company company)
      throws AxelorException {
    Map<String, Object> values = super.getDefaultValues(purchaseRequest, company);
    if (appSupplychainService.isApp("supplychain")) {
      purchaseRequest.setStockLocation(
          purchaseOrderSupplychainService.getStockLocation(
              purchaseRequest.getSupplierPartner(), purchaseRequest.getCompany()));
      values.put("stockLocation", purchaseRequest.getStockLocation());
    }
    return values;
  }
}
