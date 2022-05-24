/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Budget;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderSupplychainService {

  void updateToValidatedStatus(PurchaseOrder purchaseOrder) throws AxelorException;

  void generateBudgetDistribution(PurchaseOrder purchaseOrder);

  PurchaseOrder createPurchaseOrder(
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
      throws AxelorException;

  PurchaseOrder mergePurchaseOrders(
      List<PurchaseOrder> purchaseOrderList,
      Currency currency,
      Partner supplierPartner,
      Company company,
      StockLocation stockLocation,
      Partner contactPartner,
      PriceList priceList,
      TradingName tradingName)
      throws AxelorException;

  void updateAmountToBeSpreadOverTheTimetable(PurchaseOrder purchaseOrder);

  void applyToallBudgetDistribution(PurchaseOrder purchaseOrder);

  void isBudgetExceeded(Budget budget, BigDecimal amount) throws AxelorException;

  void setPurchaseOrderLineBudget(PurchaseOrder purchaseOrder);

  String createShipmentCostLine(PurchaseOrder purchaseOrder) throws AxelorException;

  PurchaseOrderLine createShippingCostLine(PurchaseOrder purchaseOrder, Product shippingCostProduct)
      throws AxelorException;

  boolean alreadyHasShippingCostLine(PurchaseOrder purchaseOrder, Product shippingCostProduct);

  String removeShipmentCostLine(PurchaseOrder purchaseOrder);

  BigDecimal computeExTaxTotalWithoutShippingLines(PurchaseOrder purchaseOrder);
}
