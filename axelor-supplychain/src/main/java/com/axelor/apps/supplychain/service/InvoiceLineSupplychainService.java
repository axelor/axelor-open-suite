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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceLineSupplychainService extends InvoiceLineServiceImpl {

  protected PurchaseProductService purchaseProductService;

  @Inject protected SupplierCatalogService supplierCatalogService;

  @Inject
  public InvoiceLineSupplychainService(
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      PurchaseProductService purchaseProductService) {

    super(
        currencyService,
        priceListService,
        appAccountService,
        analyticMoveLineService,
        accountManagementAccountService);
    this.purchaseProductService = purchaseProductService;
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
        return product.getPurchasesUnit();
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

  @Override
  public Map<String, Object> fillPriceAndAccount(
      Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase) throws AxelorException {
    return super.fillPriceAndAccount(invoice, invoiceLine, isPurchase);
  }

  @Override
  public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return super.fillProductInformation(invoice, invoiceLine);
    }

    Map<String, Object> productInformation = new HashMap<>();
    Integer sequence = invoiceLine.getSequence();
    if (sequence == null) {
      sequence = 0;
    }
    if (sequence == 0 && invoice.getInvoiceLineList() != null) {
      sequence = invoice.getInvoiceLineList().size();
      invoiceLine.setSequence(sequence);
    }

    productInformation.put("typeSelect", InvoiceLineRepository.TYPE_NORMAL);
    invoiceLine.setTypeSelect(InvoiceLineRepository.TYPE_NORMAL);
    productInformation.putAll(super.fillProductInformation(invoice, invoiceLine));

    return productInformation;
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

  /**
   * To create standard InvoiceLine from standard PackLine
   *
   * @param packLine
   * @param invoice
   * @param packQty
   * @param sequence
   * @return
   * @throws AxelorException
   */
  public List<InvoiceLine> createInvoiceLine(
      PackLine packLine, Invoice invoice, BigDecimal packQty, Integer sequence)
      throws AxelorException {

    Product product = packLine.getProduct();
    BigDecimal qty = packLine.getQuantity().multiply(packQty);
    boolean isPurchase = InvoiceToolService.isPurchase(invoice);
    String description = null;

    if ((isPurchase
            && appAccountService.getAppInvoice().getIsEnabledProductDescriptionCopyForCustomers())
        || (!isPurchase
            && appAccountService
                .getAppInvoice()
                .getIsEnabledProductDescriptionCopyForSuppliers())) {
      description = packLine.getProduct().getDescription();
    }

    TaxLine taxLine =
        accountManagementAccountService.getTaxLine(
            appAccountService.getTodayDate(),
            product,
            invoice.getCompany(),
            invoice.getPartner().getFiscalPosition(),
            isPurchase);
    BigDecimal price = null;
    Currency productCurrency;

    if (isPurchase) {
      price = product.getPurchasePrice();
      productCurrency = product.getPurchaseCurrency();
    } else {
      price = product.getSalePrice();
      productCurrency = product.getSaleCurrency();
    }

    if (Boolean.TRUE.equals(product.getInAti())) {
      price = this.convertUnitPrice(product.getInAti(), taxLine, price);
    }
    price =
        currencyService
            .getAmountCurrencyConvertedAtDate(
                productCurrency, invoice.getCurrency(), price, invoice.getInvoiceDate())
            .setScale(appAccountService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);

    BigDecimal inTaxPrice = this.convertUnitPrice(false, taxLine, price);
    BigDecimal unitPrice = invoice.getInAti() ? inTaxPrice : price;

    PriceListLine priceListLine =
        priceListService.getPriceListLine(product, qty, invoice.getPriceList(), unitPrice);
    BigDecimal priceDiscounted = unitPrice;
    BigDecimal discountAmount = BigDecimal.ZERO;
    int discountTypeSelect = PriceListLineRepository.AMOUNT_TYPE_NONE;

    if (priceListLine != null) {
      priceDiscounted =
          priceListService.computeDiscount(
              unitPrice, priceListLine.getAmountTypeSelect(), priceListLine.getAmount());
      discountAmount = priceListLine.getAmount();
      discountTypeSelect = priceListLine.getAmountTypeSelect();
    }

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            packLine.getProduct(),
            packLine.getProductName(),
            price,
            inTaxPrice,
            priceDiscounted,
            description,
            qty,
            packLine.getUnit(),
            taxLine,
            sequence,
            discountAmount,
            discountTypeSelect,
            null,
            null,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };
    return invoiceLineGenerator.creates();
  }

  /**
   * To create title InvoiceLine from title PackLine
   *
   * @param packLine
   * @param invoice
   * @param packQty
   * @param sequence
   * @return
   */
  public List<InvoiceLine> createTitleInvoiceLine(
      PackLine packLine, Invoice invoice, BigDecimal packQty, Integer sequence)
      throws AxelorException {

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            null,
            packLine.getProductName(),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            null,
            packQty.multiply(packLine.getQuantity()),
            null,
            null,
            sequence,
            BigDecimal.ZERO,
            PriceListLineRepository.AMOUNT_TYPE_NONE,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setTypeSelect(InvoiceLineRepository.TYPE_TITLE);
            invoiceLine.setIsShowTotal(packLine.getIsShowTotal());
            invoiceLine.setIsHideUnitAmounts(packLine.getIsHideUnitAmounts());
            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };
    return invoiceLineGenerator.creates();
  }
}
