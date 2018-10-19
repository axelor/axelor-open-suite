/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.SupplierCatalogRepository;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceLineSupplychainService extends InvoiceLineServiceImpl {

  protected PurchaseProductService purchaseProductService;

  @Inject protected AppBaseService appBaseService;

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

    Map<String, Object> info = null;

    List<SupplierCatalog> supplierCatalogList = invoiceLine.getProduct().getSupplierCatalogList();
    if (supplierCatalogList != null && !supplierCatalogList.isEmpty()) {
      SupplierCatalog supplierCatalog =
          Beans.get(SupplierCatalogRepository.class)
              .all()
              .filter(
                  "self.product = ?1 AND self.minQty <= ?2 AND self.supplierPartner = ?3 ORDER BY self.minQty DESC",
                  invoiceLine.getProduct(),
                  invoiceLine.getQty(),
                  invoice.getPartner())
              .fetchOne();

      if (supplierCatalog != null) {
        info = new HashMap<>();
        info.put(
            "price",
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    supplierCatalog.getSupplierPartner().getCurrency(),
                    invoice.getCurrency(),
                    supplierCatalog.getPrice(),
                    invoice.getInvoiceDate())
                .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
        info.put("productName", supplierCatalog.getProductSupplierName());
      } else if (getSupplierCatalog(invoiceLine.getProduct(), invoice.getPartner()) != null) {
        info = new HashMap<>();
        info.put(
            "price",
            currencyService
                .getAmountCurrencyConvertedAtDate(
                    invoiceLine.getProduct().getPurchaseCurrency(),
                    invoice.getCurrency(),
                    invoiceLine.getProduct().getPurchasePrice(),
                    invoice.getInvoiceDate())
                .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
        info.put("productName", null);
      }
    }

    return info;
  }

  private SupplierCatalog getSupplierCatalog(Product product, Partner supplierPartner) {

    if (product != null && product.getSupplierCatalogList() != null) {
      SupplierCatalog resSupplierCatalog = null;

      for (SupplierCatalog supplierCatalog : product.getSupplierCatalogList()) {
        if (supplierCatalog.getSupplierPartner().equals(supplierPartner)) {
          resSupplierCatalog =
              (resSupplierCatalog == null
                      || resSupplierCatalog.getMinQty().compareTo(supplierCatalog.getMinQty()) > 0)
                  ? supplierCatalog
                  : resSupplierCatalog;
        }
      }

      return resSupplierCatalog;
    }
    return null;
  }
}
