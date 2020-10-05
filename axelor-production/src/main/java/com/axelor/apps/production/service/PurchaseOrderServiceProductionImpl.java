/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.service.BudgetSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderStockService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class PurchaseOrderServiceProductionImpl extends PurchaseOrderServiceSupplychainImpl {

  @Inject
  public PurchaseOrderServiceProductionImpl(
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      AppBaseService appBaseService,
      PurchaseOrderStockService purchaseOrderStockService,
      BudgetSupplychainService budgetSupplychainService) {
    super(
        appSupplychainService,
        accountConfigService,
        appAccountService,
        appBaseService,
        purchaseOrderStockService,
        budgetSupplychainService);
  }

  @SuppressWarnings("unchecked")
  @Override
  @Transactional
  public PurchaseOrder mergePurchaseOrders(
      List<PurchaseOrder> purchaseOrderList,
      Currency currency,
      Partner supplierPartner,
      Company company,
      Partner contactPartner,
      PriceList priceList,
      TradingName tradingName)
      throws AxelorException {

    PurchaseOrder mergedPurchaseOrder =
        super.mergePurchaseOrders(
            purchaseOrderList,
            currency,
            supplierPartner,
            company,
            contactPartner,
            priceList,
            tradingName);

    this.setMergedPurchaseOrderForManufOrder(mergedPurchaseOrder, purchaseOrderList);
    return mergedPurchaseOrder;
  }

  @Override
  public PurchaseOrder mergePurchaseOrders(
      List<PurchaseOrder> purchaseOrderList,
      Currency currency,
      Partner supplierPartner,
      Company company,
      StockLocation stockLocation,
      Partner contactPartner,
      PriceList priceList,
      TradingName tradingName)
      throws AxelorException {

    PurchaseOrder mergedPurchaseOrder =
        super.mergePurchaseOrders(
            purchaseOrderList,
            currency,
            supplierPartner,
            company,
            stockLocation,
            contactPartner,
            priceList,
            tradingName);

    this.setMergedPurchaseOrderForManufOrder(mergedPurchaseOrder, purchaseOrderList);
    return mergedPurchaseOrder;
  }

  @SuppressWarnings("unchecked")
  private void setMergedPurchaseOrderForManufOrder(
      PurchaseOrder mergedPurchaseOrder, List<PurchaseOrder> purchaseOrderList) {

    AppProductionService appProductionService = Beans.get(AppProductionService.class);

    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getManageOutsourcing()) {

      ManufOrderRepository manufOrderRepository = Beans.get(ManufOrderRepository.class);
      for (PurchaseOrder purchaseOrder : purchaseOrderList) {

        List<ManufOrder> manufOrderList =
            (List<ManufOrder>)
                manufOrderRepository
                    .all()
                    .filter("self.purchaseOrder.id = ?1", purchaseOrder.getId());

        for (ManufOrder manufOrder : manufOrderList) {
          manufOrder.setPurchaseOrder(mergedPurchaseOrder);
          manufOrderRepository.save(manufOrder);
        }
      }
    }
  }
}
