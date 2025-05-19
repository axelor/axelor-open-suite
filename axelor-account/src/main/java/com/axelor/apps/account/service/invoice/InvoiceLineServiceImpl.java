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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.attributes.InvoiceLineAttrsService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductPriceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.common.ObjectUtils;
import com.axelor.studio.db.AppInvoice;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceLineServiceImpl implements InvoiceLineService {

  protected AccountManagementAccountService accountManagementAccountService;
  protected CurrencyService currencyService;
  protected PriceListService priceListService;
  protected AppAccountService appAccountService;
  protected ProductCompanyService productCompanyService;
  protected InvoiceLineRepository invoiceLineRepo;
  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;
  protected InvoiceLineAnalyticService invoiceLineAnalyticService;
  protected TaxAccountService taxAccountService;
  protected InternationalService internationalService;
  protected InvoiceLineAttrsService invoiceLineAttrsService;
  protected CurrencyScaleService currencyScaleService;
  protected ProductPriceService productPriceService;
  protected FiscalPositionService fiscalPositionService;
  protected InvoiceLineCheckService invoiceLineCheckService;

  @Inject
  public InvoiceLineServiceImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AccountManagementAccountService accountManagementAccountService,
      ProductCompanyService productCompanyService,
      InvoiceLineRepository invoiceLineRepo,
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      InvoiceLineAnalyticService invoiceLineAnalyticService,
      TaxAccountService taxAccountService,
      InternationalService internationalService,
      InvoiceLineAttrsService invoiceLineAttrsService,
      CurrencyScaleService currencyScaleService,
      ProductPriceService productPriceService,
      FiscalPositionService fiscalPositionService,
      InvoiceLineCheckService invoiceLineCheckService) {
    this.accountManagementAccountService = accountManagementAccountService;
    this.currencyService = currencyService;
    this.priceListService = priceListService;
    this.appAccountService = appAccountService;
    this.productCompanyService = productCompanyService;
    this.invoiceLineRepo = invoiceLineRepo;
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.invoiceLineAnalyticService = invoiceLineAnalyticService;
    this.taxAccountService = taxAccountService;
    this.internationalService = internationalService;
    this.invoiceLineAttrsService = invoiceLineAttrsService;
    this.currencyScaleService = currencyScaleService;
    this.productPriceService = productPriceService;
    this.fiscalPositionService = fiscalPositionService;
    this.invoiceLineCheckService = invoiceLineCheckService;
  }

  @Override
  public Set<TaxLine> getTaxLineSet(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase)
      throws AxelorException {

    FiscalPosition fiscalPosition = invoice.getFiscalPosition();

    return accountManagementAccountService.getTaxLineSet(
        appAccountService.getTodayDate(invoice.getCompany()),
        invoiceLine.getProduct(),
        invoice.getCompany(),
        fiscalPosition,
        isPurchase);
  }

  @Override
  public BigDecimal getExTaxUnitPrice(
      Invoice invoice, InvoiceLine invoiceLine, Set<TaxLine> taxLineSet, boolean isPurchase)
      throws AxelorException {

    return this.getUnitPrice(invoice, invoiceLine, taxLineSet, isPurchase, false);
  }

  @Override
  public BigDecimal getInTaxUnitPrice(
      Invoice invoice, InvoiceLine invoiceLine, Set<TaxLine> taxLineSet, boolean isPurchase)
      throws AxelorException {

    return currencyScaleService.getScaledValue(
        invoice, this.getUnitPrice(invoice, invoiceLine, taxLineSet, isPurchase, true));
  }

  /**
   * A function used to get the unit price of an invoice line, either in ati or wt
   *
   * @param invoice the invoice containing the invoice line
   * @param invoiceLine
   * @param taxLineSet the tax line applied to the unit price
   * @param isPurchase
   * @param resultInAti whether or not you want the result unit price in ati
   * @return the unit price of the invoice line
   * @throws AxelorException
   */
  protected BigDecimal getUnitPrice(
      Invoice invoice,
      InvoiceLine invoiceLine,
      Set<TaxLine> taxLineSet,
      boolean isPurchase,
      boolean resultInAti)
      throws AxelorException {
    Product product = invoiceLine.getProduct();
    Company company = invoice.getCompany();

    if (isPurchase) {
      return productPriceService.getPurchaseUnitPrice(
          company,
          product,
          taxLineSet,
          resultInAti,
          invoice.getInvoiceDate(),
          invoice.getCurrency());

    } else {
      return productPriceService.getSaleUnitPrice(
          company,
          product,
          taxLineSet,
          resultInAti,
          invoice.getInvoiceDate(),
          invoice.getCurrency());
    }
  }

  @Override
  public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, Invoice invoice)
      throws AxelorException {

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            invoice.getCurrency(),
            invoice.getPartner().getCurrency(),
            exTaxTotal,
            invoice.getInvoiceDate())
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, Invoice invoice)
      throws AxelorException {

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            invoice.getCurrency(),
            invoice.getCompany().getCurrency(),
            exTaxTotal,
            invoice.getInvoiceDate())
        .setScale(currencyScaleService.getCompanyScale(invoice), RoundingMode.HALF_UP);
  }

  @Override
  public PriceListLine getPriceListLine(
      InvoiceLine invoiceLine, PriceList priceList, BigDecimal price) {

    return priceListService.getPriceListLine(
        invoiceLine.getProduct(), invoiceLine.getQty(), priceList, price);
  }

  @Override
  public BigDecimal computeDiscount(InvoiceLine invoiceLine, Boolean inAti) {

    BigDecimal unitPrice = inAti ? invoiceLine.getInTaxPrice() : invoiceLine.getPrice();

    return priceListService.computeDiscount(
        unitPrice, invoiceLine.getDiscountTypeSelect(), invoiceLine.getDiscountAmount());
  }

  @Override
  public Map<String, Object> getDiscount(Invoice invoice, InvoiceLine invoiceLine, BigDecimal price)
      throws AxelorException {

    Map<String, Object> rawDiscounts = getDiscountsFromPriceLists(invoice, invoiceLine, price);

    Map<String, Object> processedDiscounts = new HashMap<>();

    if (rawDiscounts != null) {
      if (rawDiscounts.get("price") != null) {
        price = (BigDecimal) rawDiscounts.get("price");
      }
      if (invoiceLine.getProduct().getInAti() != invoice.getInAti()
          && (Integer) rawDiscounts.get("discountTypeSelect")
              != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
        processedDiscounts.put(
            "discountAmount",
            taxAccountService.convertUnitPrice(
                invoiceLine.getProduct().getInAti(),
                invoiceLine.getTaxLineSet(),
                (BigDecimal) rawDiscounts.get("discountAmount"),
                appBaseService.getNbDecimalDigitForUnitPrice()));
      } else {
        processedDiscounts.put("discountAmount", rawDiscounts.get("discountAmount"));
      }
      processedDiscounts.put("discountTypeSelect", rawDiscounts.get("discountTypeSelect"));
    }

    if (price.compareTo(
            invoiceLine.getProduct().getInAti()
                ? invoiceLine.getInTaxPrice()
                : invoiceLine.getPrice())
        != 0) {
      if (invoiceLine.getProduct().getInAti()) {
        processedDiscounts.put(
            "inTaxPrice", currencyScaleService.getScaledValue(invoiceLine, price));
        processedDiscounts.put(
            "price",
            taxAccountService.convertUnitPrice(
                true,
                invoiceLine.getTaxLineSet(),
                price,
                appBaseService.getNbDecimalDigitForUnitPrice()));
      } else {
        processedDiscounts.put("price", price);
        processedDiscounts.put(
            "inTaxPrice",
            currencyScaleService.getScaledValue(
                invoiceLine,
                taxAccountService.convertUnitPrice(
                    false,
                    invoiceLine.getTaxLineSet(),
                    price,
                    appAccountService.getNbDecimalDigitForUnitPrice())));
      }
    }

    return processedDiscounts;
  }

  @Override
  public Map<String, Object> getDiscountsFromPriceLists(
      Invoice invoice, InvoiceLine invoiceLine, BigDecimal price) {

    Map<String, Object> discounts = null;

    PriceList priceList = invoice.getPriceList();

    if (priceList != null) {
      PriceListLine priceListLine = this.getPriceListLine(invoiceLine, priceList, price);
      discounts = priceListService.getReplacedPriceAndDiscounts(priceList, priceListLine, price);
    }

    return discounts;
  }

  @Override
  public int getDiscountTypeSelect(Invoice invoice, InvoiceLine invoiceLine, BigDecimal price) {
    PriceList priceList = invoice.getPriceList();
    if (priceList != null) {
      PriceListLine priceListLine = this.getPriceListLine(invoiceLine, priceList, price);

      return priceListLine.getTypeSelect();
    }
    return 0;
  }

  @Override
  public Unit getUnit(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase)
      throws AxelorException {
    return invoiceLine.getProduct().getUnit();
  }

  @Override
  public Map<String, Object> resetProductInformation(Invoice invoice) throws AxelorException {
    Map<String, Object> productInformation = new HashMap<>();
    productInformation.put("taxLineSet", Sets.newHashSet());
    productInformation.put("taxEquiv", null);
    productInformation.put("taxCode", null);
    productInformation.put("taxRate", null);
    productInformation.put("productName", null);
    productInformation.put("unit", null);
    productInformation.put("discountAmount", null);
    productInformation.put("discountTypeSelect", PriceListLineRepository.AMOUNT_TYPE_NONE);
    productInformation.put("price", null);
    productInformation.put("inTaxPrice", null);
    productInformation.put("exTaxTotal", null);
    productInformation.put("inTaxTotal", null);
    productInformation.put("companyInTaxTotal", null);
    productInformation.put("companyExTaxTotal", null);
    productInformation.put("typeSelect", InvoiceLineRepository.TYPE_NORMAL);

    boolean isPurchase = InvoiceToolService.isPurchase(invoice);
    AppInvoice appInvoice = appAccountService.getAppInvoice();

    Boolean isEnabledProductDescriptionCopy =
        isPurchase
            ? appInvoice.getIsEnabledProductDescriptionCopyForSuppliers()
            : appInvoice.getIsEnabledProductDescriptionCopyForCustomers();

    if (isEnabledProductDescriptionCopy) {
      productInformation.put("description", null);
    }

    if (accountConfigService
            .getAccountConfig(invoice.getCompany())
            .getAnalyticDistributionTypeSelect()
        == AccountConfigRepository.DISTRIBUTION_TYPE_PRODUCT) {
      productInformation.put("analyticMoveLineList", new ArrayList<AnalyticMoveLine>());
    }
    return productInformation;
  }

  @Override
  public Map<String, Object> compute(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {
    Map<String, Object> invoiceLineMap = new HashMap<>();
    BigDecimal exTaxTotal;
    BigDecimal companyExTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal companyInTaxTotal;
    BigDecimal priceDiscounted = this.computeDiscount(invoiceLine, invoice.getInAti());
    int currencyScale = currencyScaleService.getScale(invoice);
    BigDecimal coefficient = invoiceLine.getCoefficient();

    invoiceLine.setPriceDiscounted(priceDiscounted);
    invoiceLineMap.put("priceDiscounted", invoiceLine.getPriceDiscounted());

    BigDecimal taxRate = BigDecimal.ZERO;
    Set<TaxLine> taxLineSet = invoiceLine.getTaxLineSet();
    if (CollectionUtils.isNotEmpty(taxLineSet)) {
      taxRate = taxAccountService.getTotalTaxRateInPercentage(taxLineSet);
      invoiceLine.setTaxRate(taxRate);
      invoiceLineMap.put("taxRate", invoiceLine.getTaxRate());
      invoiceLine.setTaxCode(taxAccountService.computeTaxCode(taxLineSet));
      invoiceLineMap.put("taxCode", invoiceLine.getTaxCode());
    }

    if (!invoice.getInAti()) {
      exTaxTotal =
          InvoiceLineManagement.computeAmount(
              invoiceLine.getQty(), priceDiscounted, currencyScale, coefficient);
      inTaxTotal =
          exTaxTotal
              .add(exTaxTotal.multiply(taxRate.divide(new BigDecimal(100))))
              .setScale(currencyScale, RoundingMode.HALF_UP);
    } else {
      inTaxTotal =
          InvoiceLineManagement.computeAmount(
              invoiceLine.getQty(), priceDiscounted, currencyScale, coefficient);
      exTaxTotal =
          inTaxTotal.divide(
              taxRate.divide(new BigDecimal(100)).add(BigDecimal.ONE),
              currencyScale,
              RoundingMode.HALF_UP);
    }

    companyExTaxTotal = this.getCompanyExTaxTotal(exTaxTotal, invoice);
    companyInTaxTotal = this.getCompanyExTaxTotal(inTaxTotal, invoice);

    invoiceLine.setExTaxTotal(exTaxTotal);
    invoiceLineMap.put("exTaxTotal", invoiceLine.getExTaxTotal());
    invoiceLine.setInTaxTotal(inTaxTotal);
    invoiceLineMap.put("inTaxTotal", invoiceLine.getInTaxTotal());
    invoiceLine.setCompanyInTaxTotal(companyInTaxTotal);
    invoiceLineMap.put("companyInTaxTotal", invoiceLine.getCompanyInTaxTotal());
    invoiceLine.setCompanyExTaxTotal(companyExTaxTotal);
    invoiceLineMap.put("companyExTaxTotal", invoiceLine.getCompanyExTaxTotal());

    return invoiceLineMap;
  }

  @Override
  public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {

    boolean isPurchase = InvoiceToolService.isPurchase(invoice);
    Product product = invoiceLine.getProduct();

    Map<String, Object> productInformation = fillPriceAndAccount(invoice, invoiceLine, isPurchase);
    if (productInformation.get("productName") == null
        && productInformation.get("productCode") == null) {
      productInformation.put("productName", product.getName());
      productInformation.put("productCode", product.getCode());
    }
    productInformation.put("unit", this.getUnit(invoice, invoiceLine, isPurchase));

    AppInvoice appInvoice = appAccountService.getAppInvoice();
    Boolean isEnabledProductDescriptionCopy =
        isPurchase
            ? appInvoice.getIsEnabledProductDescriptionCopyForSuppliers()
            : appInvoice.getIsEnabledProductDescriptionCopyForCustomers();

    if (isEnabledProductDescriptionCopy) {
      productInformation.put("description", product.getDescription());
    }

    return productInformation;
  }

  @Override
  public Map<String, Object> fillPriceAndAccount(
      Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase) throws AxelorException {

    Map<String, Object> productInformation = resetProductInformation(invoice);

    Product product = invoiceLine.getProduct();
    Set<TaxLine> taxLineSet = null;
    Company company = invoice.getCompany();
    FiscalPosition fiscalPosition = invoice.getFiscalPosition();

    try {
      taxLineSet = this.getTaxLineSet(invoice, invoiceLine, isPurchase);
      invoiceLineCheckService.checkInvoiceLineTaxes(taxLineSet);
      invoiceLine.setTaxLineSet(taxLineSet);
      productInformation.put("taxLineSet", taxLineSet);
      productInformation.put("taxRate", taxAccountService.getTotalTaxRateInPercentage(taxLineSet));
      productInformation.put("taxCode", taxAccountService.computeTaxCode(taxLineSet));

      TaxEquiv taxEquiv =
          accountManagementAccountService.getProductTaxEquiv(
              product, company, fiscalPosition, isPurchase);
      productInformation.put("taxEquiv", taxEquiv);

      Account account =
          accountManagementAccountService.getProductAccount(
              product, company, fiscalPosition, isPurchase, invoiceLine.getFixedAssets());
      productInformation.put("account", account);

    } catch (AxelorException e) {
      productInformation.put("error", e.getMessage());
    }

    BigDecimal price = this.getExTaxUnitPrice(invoice, invoiceLine, taxLineSet, isPurchase);
    BigDecimal inTaxPrice = this.getInTaxUnitPrice(invoice, invoiceLine, taxLineSet, isPurchase);

    productInformation.put("price", price);
    productInformation.put("inTaxPrice", inTaxPrice);

    productInformation.putAll(
        this.getDiscount(invoice, invoiceLine, product.getInAti() ? inTaxPrice : price));
    return productInformation;
  }

  @Override
  public boolean hasEndOfPackTypeLine(List<InvoiceLine> invoiceLineList) {
    return ObjectUtils.isEmpty(invoiceLineList)
        ? Boolean.FALSE
        : invoiceLineList.stream()
            .anyMatch(
                invoiceLine ->
                    invoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_END_OF_PACK);
  }

  @Override
  public boolean isStartOfPackTypeLineQtyChanged(List<InvoiceLine> invoiceLineList) {

    if (ObjectUtils.isEmpty(invoiceLineList)) {
      return false;
    }
    for (InvoiceLine invoiceLine : invoiceLineList) {
      if (invoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_START_OF_PACK
          && invoiceLine.getId() != null) {
        InvoiceLine oldInvoiceLine = invoiceLineRepo.find(invoiceLine.getId());
        if (oldInvoiceLine.getTypeSelect() == InvoiceLineRepository.TYPE_START_OF_PACK
            && invoiceLine.getQty().compareTo(oldInvoiceLine.getQty()) != 0) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public InvoiceLine updateProductQty(
      InvoiceLine invoiceLine, Invoice invoice, BigDecimal oldQty, BigDecimal newQty)
      throws AxelorException {
    BigDecimal qty =
        invoiceLine
            .getQty()
            .divide(oldQty, appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP)
            .multiply(newQty)
            .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
    invoiceLine.setQty(qty);
    if (invoiceLine.getTypeSelect() != InvoiceLineRepository.TYPE_NORMAL
        || invoiceLine.getProduct() == null) {
      return invoiceLine;
    }

    BigDecimal exTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal taxRate = BigDecimal.ZERO;
    Set<TaxLine> taxLineSet = invoiceLine.getTaxLineSet();
    BigDecimal priceDiscounted = this.computeDiscount(invoiceLine, invoice.getInAti());
    int currencyScale = currencyScaleService.getScale(invoice);
    BigDecimal coefficient = invoiceLine.getCoefficient();

    if (CollectionUtils.isNotEmpty(taxLineSet)) {
      taxRate = taxAccountService.getTotalTaxRateInPercentage(taxLineSet);
      invoiceLine.setTaxRate(taxRate);
      invoiceLine.setTaxCode(taxAccountService.computeTaxCode(taxLineSet));
    }
    if (Boolean.FALSE.equals(invoice.getInAti())) {
      exTaxTotal =
          InvoiceLineManagement.computeAmount(qty, priceDiscounted, currencyScale, coefficient);
      inTaxTotal =
          exTaxTotal
              .add(exTaxTotal.multiply(taxRate.divide(new BigDecimal(100))))
              .setScale(currencyScale, RoundingMode.HALF_UP);
    } else {
      inTaxTotal =
          InvoiceLineManagement.computeAmount(qty, priceDiscounted, currencyScale, coefficient);
      exTaxTotal =
          inTaxTotal.divide(
              taxRate.divide(new BigDecimal(100)).add(BigDecimal.ONE),
              currencyScale,
              BigDecimal.ROUND_HALF_UP);
    }
    invoiceLine.setExTaxTotal(exTaxTotal);
    invoiceLine.setCompanyExTaxTotal(this.getCompanyExTaxTotal(exTaxTotal, invoice));
    invoiceLine.setInTaxTotal(inTaxTotal);
    invoiceLine.setCompanyInTaxTotal(this.getCompanyExTaxTotal(inTaxTotal, invoice));
    invoiceLine.setPriceDiscounted(priceDiscounted);

    return this.computeAnalyticDistributionWithUpdatedQty(invoiceLine);
  }

  protected InvoiceLine computeAnalyticDistributionWithUpdatedQty(InvoiceLine invoiceLine) {

    if (appAccountService.getAppAccount().getManageAnalyticAccounting()) {
      List<AnalyticMoveLine> analyticMoveLineList =
          invoiceLineAnalyticService.computeAnalyticDistribution(invoiceLine);
      if (ObjectUtils.notEmpty(analyticMoveLineList)) {
        invoiceLine.setAnalyticMoveLineList(analyticMoveLineList);
      }
    }
    return invoiceLine;
  }

  @Override
  public boolean checkAnalyticDistribution(InvoiceLine invoiceLine) {
    return invoiceLine == null
        || (CollectionUtils.isNotEmpty(invoiceLine.getAnalyticMoveLineList())
            && invoiceLineAnalyticService.validateAnalyticMoveLines(
                invoiceLine.getAnalyticMoveLineList()))
        || invoiceLine.getAccount() == null
        || !invoiceLine.getAccount().getAnalyticDistributionAuthorized()
        || !invoiceLine.getAccount().getAnalyticDistributionRequiredOnInvoiceLines();
  }

  @Override
  public boolean checkCutOffDates(InvoiceLine invoiceLine) {
    return invoiceLine == null
        || invoiceLine.getAccount() == null
        || !invoiceLine.getAccount().getManageCutOffPeriod()
        || (invoiceLine.getCutOffStartDate() != null && invoiceLine.getCutOffEndDate() != null);
  }

  @Override
  public boolean checkManageCutOffDates(InvoiceLine invoiceLine) {
    return invoiceLine.getAccount() != null && invoiceLine.getAccount().getManageCutOffPeriod();
  }

  @Override
  public void applyCutOffDates(
      InvoiceLine invoiceLine,
      Invoice invoice,
      LocalDate cutOffStartDate,
      LocalDate cutOffEndDate) {
    if (cutOffStartDate != null && cutOffEndDate != null) {
      invoiceLine.setCutOffStartDate(cutOffStartDate);
      invoiceLine.setCutOffEndDate(cutOffEndDate);
    }
  }

  public List<InvoiceLine> updateLinesAfterFiscalPositionChange(Invoice invoice)
      throws AxelorException {
    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();

    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return null;
    }

    for (InvoiceLine invoiceLine : invoiceLineList) {

      // Skip line update if product is not filled
      if (invoiceLine.getProduct() == null) {
        continue;
      }

      FiscalPosition fiscalPosition = invoice.getFiscalPosition();
      boolean isPurchase = InvoiceToolService.isPurchase(invoice);

      Set<TaxLine> taxLineSet = this.getTaxLineSet(invoice, invoiceLine, isPurchase);
      invoiceLine.setTaxLineSet(taxLineSet);
      invoiceLine.setTaxRate(taxAccountService.getTotalTaxRateInPercentage(taxLineSet));
      invoiceLine.setTaxCode(taxAccountService.computeTaxCode(taxLineSet));

      TaxEquiv taxEquiv =
          accountManagementAccountService.getProductTaxEquiv(
              invoiceLine.getProduct(), invoice.getCompany(), fiscalPosition, isPurchase);
      invoiceLine.setTaxEquiv(taxEquiv);

      Account account =
          accountManagementAccountService.getProductAccount(
              invoiceLine.getProduct(),
              invoice.getCompany(),
              fiscalPosition,
              isPurchase,
              invoiceLine.getFixedAssets());
      invoiceLine.setAccount(account);

      BigDecimal exTaxTotal = invoiceLine.getExTaxTotal();

      BigDecimal companyExTaxTotal = invoiceLine.getCompanyExTaxTotal();

      BigDecimal price = getPrice(invoice, invoiceLine);

      invoiceLine.setInTaxTotal(
          currencyScaleService.getScaledValue(
              invoice,
              taxAccountService.convertUnitPrice(
                  false, taxLineSet, exTaxTotal, appBaseService.getNbDecimalDigitForUnitPrice())));
      invoiceLine.setCompanyInTaxTotal(
          currencyScaleService.getCompanyScaledValue(
              invoice,
              taxAccountService.convertUnitPrice(
                  false,
                  taxLineSet,
                  companyExTaxTotal,
                  appBaseService.getNbDecimalDigitForUnitPrice())));
      invoiceLine.setInTaxPrice(
          currencyScaleService.getScaledValue(
              invoice,
              taxAccountService.convertUnitPrice(
                  false, taxLineSet, price, appBaseService.getNbDecimalDigitForUnitPrice())));
    }
    return invoiceLineList;
  }

  protected BigDecimal getPrice(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException {
    BigDecimal price = null;
    if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND) {
      price =
          (BigDecimal)
              productCompanyService.get(
                  invoiceLine.getProduct(), "purchasePrice", invoice.getCompany());
    }
    if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE
        || invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND) {
      price =
          (BigDecimal)
              productCompanyService.get(
                  invoiceLine.getProduct(), "salePrice", invoice.getCompany());
    }
    return price;
  }

  @Override
  public Map<String, String> getProductDescriptionAndNameTranslation(
      Invoice invoice, InvoiceLine invoiceLine) throws AxelorException {

    Product product = invoiceLine.getProduct();

    if (product == null) {
      return Collections.emptyMap();
    }

    return internationalService.getProductDescriptionAndNameTranslation(
        product, invoice.getPartner());
  }

  @Override
  public BigDecimal getInTaxPrice(InvoiceLine invoiceLine) {
    if (invoiceLine.getPrice() == null) {
      return BigDecimal.ZERO;
    }

    Set<TaxLine> taxLineSet =
        taxAccountService.getNotNonDeductibleTaxesSet(invoiceLine.getTaxLineSet());
    BigDecimal taxValue =
        Optional.of(taxLineSet)
            .map(taxAccountService::getTotalTaxRateInPercentage)
            .map(
                it ->
                    it.multiply(invoiceLine.getPrice())
                        .divide(new BigDecimal(100), RoundingMode.HALF_UP))
            .orElse(BigDecimal.ZERO);

    return currencyScaleService.getScaledValue(invoiceLine, invoiceLine.getPrice().add(taxValue));
  }

  @Override
  public Map<String, Map<String, Object>> setScale(InvoiceLine invoiceLine, Invoice invoice) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    invoiceLineAttrsService.addInTaxPriceScale(invoice, attrsMap, "");
    invoiceLineAttrsService.addExTaxTotalScale(invoice, attrsMap, "");
    invoiceLineAttrsService.addInTaxTotalScale(invoice, attrsMap, "");
    invoiceLineAttrsService.addCompanyExTaxTotalScale(invoice, attrsMap, "");
    invoiceLineAttrsService.addCompanyInTaxTotalScale(invoice, attrsMap, "");
    invoiceLineAttrsService.addCoefficientScale(invoice, attrsMap, "");

    return attrsMap;
  }

  @Override
  public Map<String, Object> recomputeTax(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {
    Set<TaxLine> taxLineSet = invoiceLine.getTaxLineSet();
    TaxEquiv taxEquiv = null;
    FiscalPosition fiscalPosition = invoice.getFiscalPosition();

    taxAccountService.checkTaxLinesNotOnlyNonDeductibleTaxes(taxLineSet);
    taxAccountService.checkSumOfNonDeductibleTaxesOnTaxLines(taxLineSet);

    Map<String, Object> valuesMap = new HashMap<>();
    if (fiscalPosition == null || CollectionUtils.isEmpty(taxLineSet)) {
      valuesMap.put("taxEquiv", taxEquiv);
      return valuesMap;
    }
    taxEquiv = fiscalPositionService.getTaxEquivFromOrToTaxSet(fiscalPosition, taxLineSet);
    if (taxEquiv != null) {
      taxLineSet =
          taxAccountService.getTaxLineSet(
              taxEquiv.getToTaxSet(), appAccountService.getTodayDate(invoice.getCompany()));
    }

    valuesMap.put("taxLineSet", taxLineSet);
    valuesMap.put("taxEquiv", taxEquiv);
    return valuesMap;
  }
}
