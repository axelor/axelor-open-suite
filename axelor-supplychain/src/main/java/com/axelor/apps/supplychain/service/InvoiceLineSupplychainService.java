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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineServiceImpl;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class InvoiceLineSupplychainService extends InvoiceLineServiceImpl {

  protected PurchaseProductService purchaseProductService;

  @Inject protected AppBaseService appBaseService;

  @Inject protected SupplierCatalogService supplierCatalogService;

  @Inject
  public InvoiceLineSupplychainService(
      AccountManagementService accountManagementService,
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      PurchaseProductService purchaseProductService) {

    super(
        accountManagementService,
        currencyService,
        priceListService,
        appAccountService,
        analyticMoveLineService,
        accountManagementAccountService);
    this.purchaseProductService = purchaseProductService;
  }

  @Override
  public Unit getUnit(Product product, boolean isPurchase) {
    if (isPurchase) {
      if (product.getPurchasesUnit() != null) {
        return product.getPurchasesUnit();
      } else {
        return product.getUnit();
      }
    } else {
      if (product.getSalesUnit() != null) {
        return product.getPurchasesUnit();
      } else {
        return product.getUnit();
      }
    }
  }

  @Override
  public Map<String, Object> getDiscount(Invoice invoice, InvoiceLine invoiceLine, BigDecimal price)
      throws AxelorException {

    Map<String, Object> discounts = new HashMap<>();

    if (invoice.getOperationTypeSelect() < InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
      Map<String, Object> catalogInfo = this.updateInfoFromCatalog(invoice, invoiceLine);

      if (catalogInfo != null) {
        if (catalogInfo.get("price") != null) {
          price = (BigDecimal) catalogInfo.get("price");
        }
        discounts.put("productName", catalogInfo.get("productName"));
      }
    }

    discounts.putAll(super.getDiscount(invoice, invoiceLine, price));

    return discounts;
  }

  private Map<String, Object> updateInfoFromCatalog(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {

    return supplierCatalogService.updateInfoFromCatalog(
        invoiceLine.getProduct(),
        invoiceLine.getQty(),
        invoice.getPartner(),
        invoice.getCurrency(),
        invoice.getInvoiceDate());
  }
}
