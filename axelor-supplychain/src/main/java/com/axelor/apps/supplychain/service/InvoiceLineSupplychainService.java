/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceLineServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceLineSupplychainService extends InvoiceLineServiceImpl {

  protected SupplierCatalogService supplierCatalogService;

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
      TaxService taxService,
      InternationalService internationalService) {
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
        taxService,
        internationalService);
    this.supplierCatalogService = supplierCatalogService;
  }

  @Override
  public Unit getUnit(Product product, boolean isPurchase) {
    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return super.getUnit(product, isPurchase);
    }

    if (isPurchase) {
      if (product.getPurchasesUnit() != null) {
        return product.getPurchasesUnit();
      } else {
        return product.getUnit();
      }
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
      Map<String, Object> catalogInfo = this.updateInfoFromCatalog(invoice, invoiceLine);

      if (catalogInfo != null) {
        if (catalogInfo.get("price") != null) {
          price = (BigDecimal) catalogInfo.get("price");
        }
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
        invoice.getInvoiceDate(),
        invoice.getCompany());
  }

  @Override
  public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return super.fillProductInformation(invoice, invoiceLine);
    }

    Map<String, Object> productInformation =
        new HashMap<>(super.fillProductInformation(invoice, invoiceLine));

    computeSequence(invoice, invoiceLine);

    productInformation.put("typeSelect", InvoiceLineRepository.TYPE_NORMAL);
    invoiceLine.setTypeSelect(InvoiceLineRepository.TYPE_NORMAL);

    setSupplierCatalogInfo(invoice, invoiceLine, productInformation);

    return productInformation;
  }

  protected void setSupplierCatalogInfo(
      Invoice invoice, InvoiceLine invoiceLine, Map<String, Object> productInformation)
      throws AxelorException {
    Integer operationType = invoice.getOperationTypeSelect();
    if ((operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
            || operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)
        && supplierCatalogService.getSupplierCatalog(
                invoiceLine.getProduct(), invoice.getPartner(), invoice.getCompany())
            != null) {
      setSupplierCatalogProductInfo(productInformation, invoice, invoiceLine);
    }
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

  public void computeBudgetDistributionSumAmount(InvoiceLine invoiceLine, Invoice invoice) {
    List<BudgetDistribution> budgetDistributionList = invoiceLine.getBudgetDistributionList();
    PurchaseOrderLine purchaseOrderLine = invoiceLine.getPurchaseOrderLine();
    BigDecimal budgetDistributionSumAmount = BigDecimal.ZERO;
    LocalDate computeDate = invoice.getInvoiceDate();

    if (purchaseOrderLine != null && purchaseOrderLine.getPurchaseOrder().getOrderDate() != null) {
      computeDate = purchaseOrderLine.getPurchaseOrder().getOrderDate();
    }

    if (budgetDistributionList != null && !budgetDistributionList.isEmpty()) {

      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        budgetDistributionSumAmount =
            budgetDistributionSumAmount.add(budgetDistribution.getAmount());
        Beans.get(BudgetSupplychainService.class)
            .computeBudgetDistributionSumAmount(budgetDistribution, computeDate);
      }
    }
    invoiceLine.setBudgetDistributionSumAmount(budgetDistributionSumAmount);
  }

  protected void setSupplierCatalogProductInfo(
      Map<String, Object> productInformation, Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {
    Product product = invoiceLine.getProduct();
    Partner supplierPartner = invoice.getPartner();
    Company company = invoice.getCompany();

    Map<String, String> productSupplierInfos =
        supplierCatalogService.getProductSupplierInfos(supplierPartner, company, product);
    if (productSupplierInfos.get("productName") != null
        && !productSupplierInfos.get("productName").isEmpty()) {
      productInformation.put("productName", productSupplierInfos.get("productName"));
    }
    if (productSupplierInfos.get("productCode") != null
        && !productSupplierInfos.get("productCode").isEmpty()) {
      productInformation.put("productCode", productSupplierInfos.get("productCode"));
    }
    productInformation.put("qty", supplierCatalogService.getQty(product, supplierPartner, company));
    productInformation.put(
        "price",
        supplierCatalogService.getUnitPrice(
            product,
            supplierPartner,
            company,
            invoice.getCurrency(),
            invoice.getInvoiceDate(),
            invoiceLine.getTaxLine(),
            false));
    productInformation.put(
        "inTaxPrice",
        supplierCatalogService.getUnitPrice(
            product,
            supplierPartner,
            company,
            invoice.getCurrency(),
            invoice.getInvoiceDate(),
            invoiceLine.getTaxLine(),
            true));
  }

  @Override
  public Map<String, String> getProductDescriptionAndNameTranslation(
      Invoice invoice, InvoiceLine invoiceLine, String userLanguage) throws AxelorException {

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return super.getProductDescriptionAndNameTranslation(invoice, invoiceLine, userLanguage);
    }

    Product product = invoiceLine.getProduct();

    if (product == null
        || supplierCatalogService.getSupplierCatalog(
                product, invoice.getPartner(), invoice.getCompany())
            != null) {
      return Collections.emptyMap();
    }

    return super.getProductDescriptionAndNameTranslation(invoice, invoiceLine, userLanguage);
  }

  public void checkMinQty(
      Invoice invoice, InvoiceLine invoiceLine, ActionRequest request, ActionResponse response)
      throws AxelorException {
    Integer operationType = invoice.getOperationTypeSelect();
    if (operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || operationType == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND) {
      supplierCatalogService.checkMinQty(
          invoiceLine.getProduct(),
          invoice.getPartner(),
          invoice.getCompany(),
          invoiceLine.getQty(),
          request,
          response);
    }
  }
}
