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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderCreateServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderTypeSelectService;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderCreateServiceSupplychainImpl extends PurchaseOrderCreateServiceImpl
    implements PurchaseOrderCreateSupplychainService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountConfigService accountConfigService;

  @Inject
  public PurchaseOrderCreateServiceSupplychainImpl(
      PurchaseConfigService purchaseConfigService,
      AccountConfigService accountConfigService,
      PurchaseOrderTypeSelectService purchaseOrderTypeSelectService) {
    super(purchaseConfigService, purchaseOrderTypeSelectService);
    this.accountConfigService = accountConfigService;
  }

  @Override
  public PurchaseOrder createPurchaseOrder(
      User buyerUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate deliveryDate,
      String internalReference,
      String externalReference,
      StockLocation stockLocation,
      LocalDate orderDate,
      PriceList priceList,
      Partner supplierPartner,
      TradingName tradingName)
      throws AxelorException {
    return createPurchaseOrder(
        buyerUser,
        company,
        contactPartner,
        currency,
        deliveryDate,
        internalReference,
        externalReference,
        stockLocation,
        orderDate,
        priceList,
        supplierPartner,
        tradingName,
        null);
  }

  @Override
  public PurchaseOrder createPurchaseOrder(
      User buyerUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate deliveryDate,
      String internalReference,
      String externalReference,
      StockLocation stockLocation,
      LocalDate orderDate,
      PriceList priceList,
      Partner supplierPartner,
      TradingName tradingName,
      FiscalPosition fiscalPosition)
      throws AxelorException {

    LOG.debug(
        "Creation of a purchase order : Company = {},  External reference = {}, Supplier = {}",
        company.getName(),
        externalReference,
        supplierPartner.getFullName());

    PurchaseOrder purchaseOrder =
        super.createPurchaseOrder(
            buyerUser,
            company,
            contactPartner,
            currency,
            deliveryDate,
            internalReference,
            externalReference,
            orderDate,
            priceList,
            supplierPartner,
            tradingName,
            fiscalPosition);

    purchaseOrder.setStockLocation(stockLocation);

    purchaseOrder.setPaymentMode(supplierPartner.getOutPaymentMode());
    purchaseOrder.setPaymentCondition(supplierPartner.getOutPaymentCondition());

    if (purchaseOrder.getPaymentMode() == null) {
      purchaseOrder.setPaymentMode(
          accountConfigService.getAccountConfig(company).getOutPaymentMode());
    }

    if (purchaseOrder.getPaymentCondition() == null) {
      purchaseOrder.setPaymentCondition(
          accountConfigService.getAccountConfig(company).getDefPaymentCondition());
    }

    purchaseOrder.setTradingName(tradingName);

    return purchaseOrder;
  }
}
