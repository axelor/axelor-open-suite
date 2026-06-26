/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.purchase.request.PurchaseRequestToPoCreateServiceImpl;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import jakarta.inject.Inject;

public class PurchaseRequestToPoCreateServiceSupplychainImpl
    extends PurchaseRequestToPoCreateServiceImpl {

  protected final AccountConfigService accountConfigService;

  @Inject
  public PurchaseRequestToPoCreateServiceSupplychainImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateService purchaseOrderCreateService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      PurchaseRequestRepository purchaseRequestRepo,
      AppBaseService appBaseService,
      AccountConfigService accountConfigService) {
    super(
        purchaseOrderService,
        purchaseOrderCreateService,
        purchaseOrderLineService,
        purchaseOrderRepo,
        purchaseRequestRepo,
        appBaseService);
    this.accountConfigService = accountConfigService;
  }

  @Override
  protected PurchaseOrder createPurchaseOrder(PurchaseRequest purchaseRequest, Company company)
      throws AxelorException {
    PurchaseOrder purchaseOrder = super.createPurchaseOrder(purchaseRequest, company);
    if (appBaseService.isApp("supplychain")) {
      purchaseOrder.setStockLocation(purchaseRequest.getStockLocation());
    }
    return purchaseOrder;
  }

  @Override
  protected void setPurchaseOrderSupplierDetails(PurchaseOrder purchaseOrder)
      throws AxelorException {
    super.setPurchaseOrderSupplierDetails(purchaseOrder);
    Partner supplierPartner = purchaseOrder.getSupplierPartner();
    if (supplierPartner == null) {
      return;
    }
    purchaseOrder.setShipmentMode(supplierPartner.getShipmentMode());
    purchaseOrder.setFreightCarrierMode(supplierPartner.getFreightCarrierMode());
    purchaseOrder.setPaymentMode(supplierPartner.getOutPaymentMode());
    purchaseOrder.setPaymentCondition(supplierPartner.getOutPaymentCondition());

    if (purchaseOrder.getPaymentMode() == null) {
      purchaseOrder.setPaymentMode(
          accountConfigService.getAccountConfig(purchaseOrder.getCompany()).getOutPaymentMode());
    }
    if (purchaseOrder.getPaymentCondition() == null) {
      purchaseOrder.setPaymentCondition(
          accountConfigService
              .getAccountConfig(purchaseOrder.getCompany())
              .getDefPaymentCondition());
    }

    purchaseOrder.setCompanyBankDetails(
        Beans.get(BankDetailsService.class)
            .getDefaultCompanyBankDetails(
                purchaseOrder.getCompany(),
                purchaseOrder.getPaymentMode(),
                purchaseOrder.getSupplierPartner(),
                null));
  }

  @Override
  protected String getGroupBySupplierKey(PurchaseRequest purchaseRequest) {
    String key = super.getGroupBySupplierKey(purchaseRequest);

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return key;
    }

    StockLocation stockLocation = purchaseRequest.getStockLocation();
    if (stockLocation != null) {
      key = key + "_" + stockLocation.getId().toString();
    }
    return key;
  }
}
