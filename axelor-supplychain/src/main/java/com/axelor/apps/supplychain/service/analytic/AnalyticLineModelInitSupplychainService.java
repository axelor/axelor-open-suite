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
package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.model.AnalyticLineModel;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.common.base.Preconditions;

public class AnalyticLineModelInitSupplychainService {

  public static AnalyticLineModel castAsAnalyticLineModel(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Preconditions.checkNotNull(saleOrderLine);

    if (saleOrder == null && saleOrderLine.getSaleOrder() != null) {
      saleOrder = saleOrderLine.getSaleOrder();
    }

    Company company = null;
    TradingName tradingName = null;
    Partner partner = null;
    FiscalPosition fiscalPosition = null;

    if (saleOrder != null) {
      company = saleOrder.getCompany();
      tradingName = saleOrder.getTradingName();
      partner = saleOrder.getClientPartner();
      fiscalPosition = saleOrder.getFiscalPosition();
    }

    return new AnalyticLineModel(
        (AnalyticLine) saleOrderLine,
        saleOrderLine.getProduct(),
        null,
        company,
        tradingName,
        partner,
        false,
        saleOrderLine.getCompanyExTaxTotal(),
        fiscalPosition);
  }

  public static AnalyticLineModel castAsAnalyticLineModel(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {
    Preconditions.checkNotNull(purchaseOrderLine);
    if (purchaseOrder == null && purchaseOrderLine.getPurchaseOrder() != null) {
      purchaseOrder = purchaseOrderLine.getPurchaseOrder();
    }

    Company company = null;
    TradingName tradingName = null;
    Partner partner = null;
    FiscalPosition fiscalPosition = null;

    if (purchaseOrder != null) {
      company = purchaseOrder.getCompany();
      tradingName = purchaseOrder.getTradingName();
      partner = purchaseOrder.getSupplierPartner();
      fiscalPosition = purchaseOrder.getFiscalPosition();
    }

    return new AnalyticLineModel(
        (AnalyticLine) purchaseOrderLine,
        purchaseOrderLine.getProduct(),
        null,
        company,
        tradingName,
        partner,
        true,
        purchaseOrderLine.getCompanyExTaxTotal(),
        fiscalPosition);
  }
}
