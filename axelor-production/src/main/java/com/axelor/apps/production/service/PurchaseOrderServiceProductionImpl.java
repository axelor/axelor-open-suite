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
package com.axelor.apps.production.service;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderStockService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PurchaseOrderServiceProductionImpl extends PurchaseOrderServiceSupplychainImpl {

  protected ManufOrderRepository manufOrderRepo;
  protected AppProductionService appProductionService;

  @Inject
  public PurchaseOrderServiceProductionImpl(
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      AppBaseService appBaseService,
      PurchaseOrderStockService purchaseOrderStockService,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      PurchaseOrderLineService purchaseOrderLineService,
      ManufOrderRepository manufOrderRepo,
      AppProductionService appProductionService,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService,
      CurrencyScaleService currencyScaleService) {
    super(
        appSupplychainService,
        accountConfigService,
        appAccountService,
        appBaseService,
        purchaseOrderStockService,
        purchaseOrderLineRepository,
        purchaseOrderLineService,
        partnerStockSettingsService,
        stockConfigService,
        currencyScaleService);
    this.manufOrderRepo = manufOrderRepo;
    this.appProductionService = appProductionService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PurchaseOrder mergePurchaseOrders(
      List<PurchaseOrder> purchaseOrderList,
      Currency currency,
      Partner supplierPartner,
      Company company,
      Partner contactPartner,
      PriceList priceList,
      TradingName tradingName)
      throws AxelorException {

    Set<ManufOrder> manufOrders = this.getManufOrdersOfPurchaseOrders(purchaseOrderList);

    removePurchaseOrdersFromMO(purchaseOrderList, manufOrders);

    PurchaseOrder mergedPurchaseOrder =
        super.mergePurchaseOrders(
            purchaseOrderList,
            currency,
            supplierPartner,
            company,
            contactPartner,
            priceList,
            tradingName);

    manufOrders.forEach(manufOrder -> manufOrder.addPurchaseOrderSetItem(mergedPurchaseOrder));
    return mergedPurchaseOrder;
  }

  @Transactional(rollbackOn = {Exception.class})
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

    Set<ManufOrder> manufOrders = this.getManufOrdersOfPurchaseOrders(purchaseOrderList);

    removePurchaseOrdersFromMO(purchaseOrderList, manufOrders);

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

    manufOrders.forEach(manufOrder -> manufOrder.addPurchaseOrderSetItem(mergedPurchaseOrder));

    return mergedPurchaseOrder;
  }

  protected void removePurchaseOrdersFromMO(
      List<PurchaseOrder> purchaseOrderList, Set<ManufOrder> manufOrders) {
    for (ManufOrder manufOrder : manufOrders) {
      List<PurchaseOrder> orderstoRemove =
          manufOrder.getPurchaseOrderSet().stream()
              .filter(purchaseOrderList::contains)
              .collect(Collectors.toList());
      orderstoRemove.forEach(manufOrder::removePurchaseOrderSetItem);
    }
  }

  protected Set<ManufOrder> getManufOrdersOfPurchaseOrders(List<PurchaseOrder> purchaseOrderList) {
    Set<ManufOrder> manufOrders = new HashSet<>();
    for (PurchaseOrder purchaseOrder : purchaseOrderList) {
      manufOrders.addAll(
          manufOrderRepo
              .all()
              .filter(":purchaseOrder MEMBER OF self.purchaseOrderSet")
              .bind("purchaseOrder", purchaseOrder)
              .fetch());
    }
    return manufOrders;
  }
}
