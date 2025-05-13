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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceLineCheckService;
import com.axelor.apps.account.service.invoice.InvoiceLineServiceImpl;
import com.axelor.apps.account.service.invoice.attributes.InvoiceLineAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductPriceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InvoiceLineSupplychainService extends InvoiceLineServiceImpl {

  protected SupplierCatalogService supplierCatalogService;
  protected final InvoiceLineSupplierCatalogService invoiceLineSupplierCatalogService;

  @Inject
  public InvoiceLineSupplychainService(
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AccountManagementAccountService accountManagementAccountService,
      ProductCompanyService productCompanyService,
      InvoiceLineRepository invoiceLineRepo,
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      InvoiceLineAnalyticService invoiceLineAnalyticService,
      SupplierCatalogService supplierCatalogService,
      TaxAccountService taxAccountService,
      InternationalService internationalService,
      InvoiceLineAttrsService invoiceLineAttrsService,
      CurrencyScaleService currencyScaleService,
      ProductPriceService productPriceService,
      FiscalPositionService fiscalPositionService,
      InvoiceLineCheckService invoiceLineCheckService,
      InvoiceLineSupplierCatalogService invoiceLineSupplierCatalogService) {
    super(
        currencyService,
        priceListService,
        appAccountService,
        accountManagementAccountService,
        productCompanyService,
        invoiceLineRepo,
        appBaseService,
        accountConfigService,
        invoiceLineAnalyticService,
        taxAccountService,
        internationalService,
        invoiceLineAttrsService,
        currencyScaleService,
        productPriceService,
        fiscalPositionService,
        invoiceLineCheckService);
    this.supplierCatalogService = supplierCatalogService;
    this.invoiceLineSupplierCatalogService = invoiceLineSupplierCatalogService;
  }

  @Override
  public Unit getUnit(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase)
      throws AxelorException {
    Product product = invoiceLine.getProduct();
    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return super.getUnit(invoice, invoiceLine, isPurchase);
    }

    if (isPurchase) {
      return supplierCatalogService.getUnit(product, invoice.getPartner(), invoice.getCompany());
    } else {
      if (product.getSalesUnit() != null) {
        return product.getSalesUnit();
      } else {
        return product.getUnit();
      }
    }
  }

  @Override
  public Map<String, Object> getDiscount(Invoice invoice, InvoiceLine invoiceLine, BigDecimal price)
      throws AxelorException {

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return super.getDiscount(invoice, invoiceLine, price);
    }

    Map<String, Object> discounts = new HashMap<>();

    if (invoice.getOperationTypeSelect() < InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {
      Map<String, Object> catalogInfo =
          invoiceLineSupplierCatalogService.updateInfoFromCatalog(invoice, invoiceLine);

      if (catalogInfo != null) {
        if (catalogInfo.get("price") != null) {
          price = (BigDecimal) catalogInfo.get("price");
        }
      }
    }

    discounts.putAll(super.getDiscount(invoice, invoiceLine, price));

    return discounts;
  }

  @Override
  public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {
    Map<String, Object> productInformation =
        new HashMap<>(super.fillProductInformation(invoice, invoiceLine));

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return productInformation;
    }

    computeSequence(invoice, invoiceLine);

    productInformation.put("typeSelect", InvoiceLineRepository.TYPE_NORMAL);
    invoiceLine.setTypeSelect(InvoiceLineRepository.TYPE_NORMAL);

    invoiceLineSupplierCatalogService.setSupplierCatalogInfo(
        invoice, invoiceLine, productInformation);

    return productInformation;
  }

  protected void computeSequence(Invoice invoice, InvoiceLine invoiceLine) {
    Integer sequence = invoiceLine.getSequence();
    if (sequence == null) {
      sequence = 0;
    }
    if (sequence == 0 && invoice.getInvoiceLineList() != null) {
      sequence = invoice.getInvoiceLineList().size();
      invoiceLine.setSequence(sequence);
    }
  }

  @Override
  public Map<String, String> getProductDescriptionAndNameTranslation(
      Invoice invoice, InvoiceLine invoiceLine) throws AxelorException {

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return super.getProductDescriptionAndNameTranslation(invoice, invoiceLine);
    }

    Product product = invoiceLine.getProduct();

    if (product == null
        || invoiceLineSupplierCatalogService.getSupplierCatalog(invoice, invoiceLine) != null) {
      return Collections.emptyMap();
    }

    return super.getProductDescriptionAndNameTranslation(invoice, invoiceLine);
  }
}
