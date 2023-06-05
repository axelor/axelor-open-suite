/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.invoice.generator;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.invoice.InvoiceLineAnalyticService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

/** Classe de création de ligne de facture abstraite. */
public abstract class InvoiceLineGeneratorSupplyChain extends InvoiceLineGenerator {

  protected SaleOrderLine saleOrderLine;
  protected PurchaseOrderLine purchaseOrderLine;
  protected StockMoveLine stockMoveLine;

  protected UnitConversionService unitConversionService;

  @Inject
  public InvoiceLineGeneratorSupplyChain(
      Invoice invoice,
      Product product,
      String productName,
      BigDecimal price,
      BigDecimal inTaxPrice,
      BigDecimal priceDiscounted,
      String description,
      BigDecimal qty,
      Unit unit,
      TaxLine taxLine,
      int sequence,
      BigDecimal discountAmount,
      int discountTypeSelect,
      BigDecimal exTaxTotal,
      BigDecimal inTaxTotal,
      boolean isTaxInvoice,
      SaleOrderLine saleOrderLine,
      PurchaseOrderLine purchaseOrderLine,
      StockMoveLine stockMoveLine) {
    super(
        invoice,
        product,
        productName,
        price,
        inTaxPrice,
        priceDiscounted,
        description,
        qty,
        unit,
        taxLine,
        sequence,
        discountAmount,
        discountTypeSelect,
        exTaxTotal,
        inTaxTotal,
        isTaxInvoice);
    this.saleOrderLine = saleOrderLine;
    this.purchaseOrderLine = purchaseOrderLine;
    this.stockMoveLine = stockMoveLine;
    this.appBaseService = Beans.get(AppBaseService.class);
    this.unitConversionService = Beans.get(UnitConversionService.class);
  }

  protected InvoiceLineGeneratorSupplyChain(
      Invoice invoice,
      Product product,
      String productName,
      String description,
      BigDecimal qty,
      Unit unit,
      int sequence,
      boolean isTaxInvoice,
      SaleOrderLine saleOrderLine,
      PurchaseOrderLine purchaseOrderLine,
      StockMoveLine stockMoveLine)
      throws AxelorException {

    super(invoice, product, productName, description, qty, unit, sequence, isTaxInvoice);

    this.saleOrderLine = saleOrderLine;
    this.purchaseOrderLine = purchaseOrderLine;
    this.stockMoveLine = stockMoveLine;
    this.appBaseService = Beans.get(AppBaseService.class);
    this.unitConversionService = Beans.get(UnitConversionService.class);

    if (saleOrderLine != null) {
      this.discountAmount = saleOrderLine.getDiscountAmount();
      this.price = saleOrderLine.getPrice();
      this.inTaxPrice = saleOrderLine.getInTaxPrice();
      if (this.unit != null && !this.unit.equals(saleOrderLine.getUnit())) {
        this.qty =
            unitConversionService.convert(
                this.unit, saleOrderLine.getUnit(), qty, qty.scale(), product);
        this.unit = saleOrderLine.getUnit();
      }
      this.priceDiscounted = saleOrderLine.getPriceDiscounted();
      this.taxLine = saleOrderLine.getTaxLine();
      this.discountTypeSelect = saleOrderLine.getDiscountTypeSelect();
      this.typeSelect = saleOrderLine.getTypeSelect();
    } else if (purchaseOrderLine != null) {
      if (purchaseOrderLine.getIsTitleLine()) {
        this.typeSelect = InvoiceLineRepository.TYPE_TITLE;
      }
      this.discountAmount = purchaseOrderLine.getDiscountAmount();
      this.price = purchaseOrderLine.getPrice();
      this.inTaxPrice = purchaseOrderLine.getInTaxPrice();
      if (this.unit != null && !this.unit.equals(purchaseOrderLine.getUnit())) {
        this.qty =
            unitConversionService.convert(
                this.unit, purchaseOrderLine.getUnit(), qty, qty.scale(), product);
        this.unit = purchaseOrderLine.getUnit();
      }
      this.priceDiscounted = purchaseOrderLine.getPriceDiscounted();
      this.taxLine = purchaseOrderLine.getTaxLine();
      this.discountTypeSelect = purchaseOrderLine.getDiscountTypeSelect();
    } else if (stockMoveLine != null) {
      this.priceDiscounted = stockMoveLine.getUnitPriceUntaxed();
      Unit saleOrPurchaseUnit = this.getSaleOrPurchaseUnit();

      if (saleOrPurchaseUnit != null
          && this.unit != null
          && !this.unit.equals(saleOrPurchaseUnit)) {
        this.qty =
            unitConversionService.convert(
                this.unit, saleOrPurchaseUnit, qty, qty.scale(), stockMoveLine.getProduct());
        this.priceDiscounted =
            unitConversionService.convert(
                this.unit,
                saleOrPurchaseUnit,
                this.priceDiscounted,
                appBaseService.getNbDecimalDigitForUnitPrice(),
                product);
        this.unit = saleOrPurchaseUnit;
      }
    }
  }

  /**
   * @return
   * @throws AxelorException
   */
  @Override
  protected InvoiceLine createInvoiceLine() throws AxelorException {

    InvoiceLine invoiceLine = super.createInvoiceLine();

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return invoiceLine;
    }

    InvoiceLineAnalyticService invoiceLineAnalyticService =
        Beans.get(InvoiceLineAnalyticService.class);

    this.assignOriginElements(invoiceLine);

    List<AnalyticMoveLine> analyticMoveLineList = null;

    if (saleOrderLine != null) {

      switch (saleOrderLine.getTypeSelect()) {
        case SaleOrderLineRepository.TYPE_END_OF_PACK:
          invoiceLine.setIsHideUnitAmounts(saleOrderLine.getIsHideUnitAmounts());
          invoiceLine.setIsShowTotal(saleOrderLine.getIsShowTotal());
          break;

        case SaleOrderLineRepository.TYPE_NORMAL:
          if (saleOrderLine.getAnalyticDistributionTemplate() != null
              || !ObjectUtils.isEmpty(saleOrderLine.getAnalyticMoveLineList())) {
            invoiceLine.setAnalyticDistributionTemplate(
                saleOrderLine.getAnalyticDistributionTemplate());
            this.copyAnalyticMoveLines(saleOrderLine.getAnalyticMoveLineList(), invoiceLine);
            analyticMoveLineList =
                invoiceLineAnalyticService.computeAnalyticDistribution(invoiceLine);
          } else {
            analyticMoveLineList =
                invoiceLineAnalyticService.getAndComputeAnalyticDistribution(invoiceLine, invoice);
            analyticMoveLineList.stream().forEach(invoiceLine::addAnalyticMoveLineListItem);
          }
          break;

        default:
          return invoiceLine;
      }

    } else if (purchaseOrderLine != null) {
      if (purchaseOrderLine.getIsTitleLine()) {
        return invoiceLine;
      } else {
        if (purchaseOrderLine.getAnalyticDistributionTemplate() != null
            || !ObjectUtils.isEmpty(purchaseOrderLine.getAnalyticMoveLineList())) {
          invoiceLine.setAnalyticDistributionTemplate(
              purchaseOrderLine.getAnalyticDistributionTemplate());
          this.copyAnalyticMoveLines(purchaseOrderLine.getAnalyticMoveLineList(), invoiceLine);
          analyticMoveLineList =
              invoiceLineAnalyticService.computeAnalyticDistribution(invoiceLine);
        } else {
          analyticMoveLineList =
              invoiceLineAnalyticService.getAndComputeAnalyticDistribution(invoiceLine, invoice);
          analyticMoveLineList.stream().forEach(invoiceLine::addAnalyticMoveLineListItem);
        }

        this.copyBudgetDistributionList(purchaseOrderLine.getBudgetDistributionList(), invoiceLine);
        invoiceLine.setBudget(purchaseOrderLine.getBudget());
        invoiceLine.setBudgetDistributionSumAmount(
            purchaseOrderLine.getBudgetDistributionSumAmount());
        invoiceLine.setFixedAssets(purchaseOrderLine.getFixedAssets());

        if (product != null && purchaseOrderLine.getFixedAssets()) {
          FixedAssetCategory fixedAssetCategory =
              accountManagementService.getProductFixedAssetCategory(product, invoice.getCompany());
          invoiceLine.setFixedAssetCategory(fixedAssetCategory);
        }
      }
    } else if (stockMoveLine != null) {

      this.price = stockMoveLine.getUnitPriceUntaxed();
      this.inTaxPrice = stockMoveLine.getUnitPriceTaxed();

      this.price =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              this.unit,
              this.price,
              appBaseService.getNbDecimalDigitForUnitPrice(),
              product);
      this.inTaxPrice =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              this.unit,
              this.inTaxPrice,
              appBaseService.getNbDecimalDigitForUnitPrice(),
              product);

      invoiceLine.setPrice(price);
      invoiceLine.setInTaxPrice(inTaxPrice);

      analyticMoveLineList =
          invoiceLineAnalyticService.getAndComputeAnalyticDistribution(invoiceLine, invoice);
      analyticMoveLineList.stream().forEach(invoiceLine::addAnalyticMoveLineListItem);
    }

    if (!CollectionUtils.isEmpty(invoiceLine.getAnalyticMoveLineList())) {
      for (AnalyticMoveLine analyticMoveLine : invoiceLine.getAnalyticMoveLineList()) {
        analyticMoveLine.setPurchaseOrderLine(invoiceLine.getPurchaseOrderLine());
        analyticMoveLine.setSaleOrderLine(invoiceLine.getSaleOrderLine());
      }
    }

    FiscalPosition fiscalPosition = invoice.getFiscalPosition();
    boolean isPurchase = InvoiceToolService.isPurchase(invoice);

    // Determine and set the account for the line
    if (product != null) {
      invoiceLine.setProductCode(
          (String) productCompanyService.get(product, "code", invoice.getCompany()));
      Account account =
          accountManagementService.getProductAccount(
              product,
              invoice.getCompany(),
              fiscalPosition,
              isPurchase,
              invoiceLine.getFixedAssets());
      invoiceLine.setAccount(account);
    }

    // Determine and set the taxEquiv for the line
    if (product != null) {
      TaxEquiv taxEquiv =
          Beans.get(AccountManagementService.class)
              .getProductTaxEquiv(product, invoice.getCompany(), fiscalPosition, isPurchase);

      invoiceLine.setTaxEquiv(taxEquiv);
    }

    return invoiceLine;
  }

  public void assignOriginElements(InvoiceLine invoiceLine) throws AxelorException {

    StockMove stockMove = null;
    if (stockMoveLine != null) {
      stockMove = stockMoveLine.getStockMove();
    }

    if (stockMove != null) {
      if (InvoiceToolService.isPurchase(invoice)) {
        invoiceLine.setIncomingStockMove(stockMove);
      } else {
        invoiceLine.setOutgoingStockMove(stockMove);
      }
    }
    if (saleOrderLine != null) {
      invoiceLine.setSaleOrderLine(saleOrderLine);
    }
    if (purchaseOrderLine != null) {
      invoiceLine.setPurchaseOrderLine(purchaseOrderLine);
    }
  }

  public void copyAnalyticMoveLines(
      List<AnalyticMoveLine> originalAnalyticMoveLineList, InvoiceLine invoiceLine) {

    if (originalAnalyticMoveLineList == null) {
      return;
    }

    for (AnalyticMoveLine originalAnalyticMoveLine : originalAnalyticMoveLineList) {

      AnalyticMoveLine analyticMoveLine =
          Beans.get(AnalyticMoveLineRepository.class).copy(originalAnalyticMoveLine, false);

      analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_INVOICE);

      invoiceLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }
  }

  public void copyBudgetDistributionList(
      List<BudgetDistribution> originalBudgetDistributionList, InvoiceLine invoiceLine) {

    if (originalBudgetDistributionList == null) {
      return;
    }

    for (BudgetDistribution budgetDistributionIt : originalBudgetDistributionList) {
      BudgetDistribution budgetDistribution = new BudgetDistribution();
      budgetDistribution.setBudget(budgetDistributionIt.getBudget());
      budgetDistribution.setAmount(budgetDistributionIt.getAmount());
      budgetDistribution.setBudgetAmountAvailable(budgetDistributionIt.getBudgetAmountAvailable());
      invoiceLine.addBudgetDistributionListItem(budgetDistribution);
    }
  }

  public Unit getSaleOrPurchaseUnit() throws AxelorException {

    if (!InvoiceToolService.isPurchase(invoice)) {
      return product.getSalesUnit();
    } else {
      return product.getPurchasesUnit();
    }
  }
}
