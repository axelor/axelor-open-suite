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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.base.db.AppInvoice;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceLineServiceImpl implements InvoiceLineService {

  protected AccountManagementAccountService accountManagementAccountService;
  protected CurrencyService currencyService;
  protected PriceListService priceListService;
  protected AppAccountService appAccountService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected ProductCompanyService productCompanyService;
  protected InvoiceLineRepository invoiceLineRepo;
  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;

  @Inject
  public InvoiceLineServiceImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      ProductCompanyService productCompanyService,
      InvoiceLineRepository invoiceLineRepo,
      AppBaseService appBaseService,
      AccountConfigService accountConfigService) {

    this.accountManagementAccountService = accountManagementAccountService;
    this.currencyService = currencyService;
    this.priceListService = priceListService;
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.productCompanyService = productCompanyService;
    this.invoiceLineRepo = invoiceLineRepo;
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public List<AnalyticMoveLine> getAndComputeAnalyticDistribution(
      InvoiceLine invoiceLine, Invoice invoice) throws AxelorException {
    if (accountConfigService
            .getAccountConfig(invoice.getCompany())
            .getAnalyticDistributionTypeSelect()
        == AccountConfigRepository.DISTRIBUTION_TYPE_FREE) {
      return MoreObjects.firstNonNull(invoiceLine.getAnalyticMoveLineList(), new ArrayList<>());
    }

    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticMoveLineService.getAnalyticDistributionTemplate(
            invoice.getPartner(), invoiceLine.getProduct(), invoice.getCompany());

    invoiceLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);

    if (invoiceLine.getAnalyticMoveLineList() != null) {
      invoiceLine.getAnalyticMoveLineList().clear();
    }

    return this.computeAnalyticDistribution(invoiceLine);
  }

  @Override
  public List<AnalyticMoveLine> computeAnalyticDistribution(InvoiceLine invoiceLine) {

    List<AnalyticMoveLine> analyticMoveLineList = invoiceLine.getAnalyticMoveLineList();

    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      return createAnalyticDistributionWithTemplate(invoiceLine);
    } else {
      LocalDate date =
          appAccountService.getTodayDate(
              invoiceLine.getInvoice() != null
                  ? invoiceLine.getInvoice().getCompany()
                  : Optional.ofNullable(AuthUtils.getUser())
                      .map(User::getActiveCompany)
                      .orElse(null));
      if (invoiceLine.getAnalyticMoveLineList() != null) {
        for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
          analyticMoveLineService.updateAnalyticMoveLine(
              analyticMoveLine, invoiceLine.getCompanyExTaxTotal(), date);
        }
      }
      return analyticMoveLineList;
    }
  }

  @Override
  public List<AnalyticMoveLine> createAnalyticDistributionWithTemplate(InvoiceLine invoiceLine) {
    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            invoiceLine.getAnalyticDistributionTemplate(),
            invoiceLine.getCompanyExTaxTotal(),
            AnalyticMoveLineRepository.STATUS_FORECAST_INVOICE,
            appAccountService.getTodayDate(
                invoiceLine.getInvoice() != null
                    ? invoiceLine.getInvoice().getCompany()
                    : Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null)));

    return analyticMoveLineList;
  }

  @Override
  public TaxLine getTaxLine(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase)
      throws AxelorException {

    FiscalPosition fiscalPosition = invoice.getFiscalPosition();

    return accountManagementAccountService.getTaxLine(
        appAccountService.getTodayDate(invoice.getCompany()),
        invoiceLine.getProduct(),
        invoice.getCompany(),
        fiscalPosition,
        isPurchase);
  }

  @Override
  public BigDecimal getExTaxUnitPrice(
      Invoice invoice, InvoiceLine invoiceLine, TaxLine taxLine, boolean isPurchase)
      throws AxelorException {

    return this.getUnitPrice(invoice, invoiceLine, taxLine, isPurchase, false);
  }

  @Override
  public BigDecimal getInTaxUnitPrice(
      Invoice invoice, InvoiceLine invoiceLine, TaxLine taxLine, boolean isPurchase)
      throws AxelorException {

    return this.getUnitPrice(invoice, invoiceLine, taxLine, isPurchase, true);
  }

  /**
   * A function used to get the unit price of an invoice line, either in ati or wt
   *
   * @param invoice the invoice containing the invoice line
   * @param invoiceLine
   * @param taxLine the tax line applied to the unit price
   * @param isPurchase
   * @param resultInAti whether or not you want the result unit price in ati
   * @return the unit price of the invoice line
   * @throws AxelorException
   */
  private BigDecimal getUnitPrice(
      Invoice invoice,
      InvoiceLine invoiceLine,
      TaxLine taxLine,
      boolean isPurchase,
      boolean resultInAti)
      throws AxelorException {
    Product product = invoiceLine.getProduct();

    BigDecimal price = null;
    Currency productCurrency;

    if (isPurchase) {
      price =
          (BigDecimal) productCompanyService.get(product, "purchasePrice", invoice.getCompany());
      productCurrency =
          (Currency) productCompanyService.get(product, "purchaseCurrency", invoice.getCompany());
    } else {
      price = (BigDecimal) productCompanyService.get(product, "salePrice", invoice.getCompany());
      productCurrency =
          (Currency) productCompanyService.get(product, "saleCurrency", invoice.getCompany());
    }

    if ((Boolean) productCompanyService.get(product, "inAti", invoice.getCompany())
        != resultInAti) {
      price =
          this.convertUnitPrice(
              (Boolean) productCompanyService.get(product, "inAti", invoice.getCompany()),
              taxLine,
              price);
    }

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            productCurrency, invoice.getCurrency(), price, invoice.getInvoiceDate())
        .setScale(appAccountService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
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
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
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
  public BigDecimal convertUnitPrice(Boolean priceIsAti, TaxLine taxLine, BigDecimal price) {

    if (taxLine == null) {
      return price;
    }

    if (priceIsAti) {
      price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    } else {
      price = price.add(price.multiply(taxLine.getValue()));
    }
    return price;
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
            this.convertUnitPrice(
                invoiceLine.getProduct().getInAti(),
                invoiceLine.getTaxLine(),
                (BigDecimal) rawDiscounts.get("discountAmount")));
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
        processedDiscounts.put("inTaxPrice", price);
        processedDiscounts.put(
            "price", this.convertUnitPrice(true, invoiceLine.getTaxLine(), price));
      } else {
        processedDiscounts.put("price", price);
        processedDiscounts.put(
            "inTaxPrice", this.convertUnitPrice(false, invoiceLine.getTaxLine(), price));
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
  public Unit getUnit(Product product, boolean isPurchase) {
    return product.getUnit();
  }

  @Override
  public Map<String, Object> resetProductInformation(Invoice invoice) throws AxelorException {
    Map<String, Object> productInformation = new HashMap<>();
    productInformation.put("taxLine", null);
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
  public void compute(Invoice invoice, InvoiceLine invoiceLine) throws AxelorException {
    BigDecimal exTaxTotal;
    BigDecimal companyExTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal companyInTaxTotal;
    BigDecimal priceDiscounted = this.computeDiscount(invoiceLine, invoice.getInAti());

    invoiceLine.setPriceDiscounted(priceDiscounted);

    BigDecimal taxRate = BigDecimal.ZERO;
    if (invoiceLine.getTaxLine() != null) {
      taxRate = invoiceLine.getTaxLine().getValue();
      invoiceLine.setTaxRate(taxRate);
      invoiceLine.setTaxCode(invoiceLine.getTaxLine().getTax().getCode());
    }

    if (!invoice.getInAti()) {
      exTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), priceDiscounted);
      inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
    } else {
      inTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), priceDiscounted);
      exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    }

    companyExTaxTotal = this.getCompanyExTaxTotal(exTaxTotal, invoice);
    companyInTaxTotal = this.getCompanyExTaxTotal(inTaxTotal, invoice);

    invoiceLine.setExTaxTotal(exTaxTotal);
    invoiceLine.setInTaxTotal(inTaxTotal);
    invoiceLine.setCompanyInTaxTotal(companyInTaxTotal);
    invoiceLine.setCompanyExTaxTotal(companyExTaxTotal);
  }

  @Override
  public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {

    boolean isPurchase = InvoiceToolService.isPurchase(invoice);
    Product product = invoiceLine.getProduct();

    Map<String, Object> productInformation = fillPriceAndAccount(invoice, invoiceLine, isPurchase);
    productInformation.put("productName", product.getName());
    productInformation.put("productCode", product.getCode());
    productInformation.put("unit", this.getUnit(product, isPurchase));

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
    TaxLine taxLine = null;
    Company company = invoice.getCompany();
    FiscalPosition fiscalPosition = invoice.getFiscalPosition();

    try {
      taxLine = this.getTaxLine(invoice, invoiceLine, isPurchase);
      invoiceLine.setTaxLine(taxLine);
      productInformation.put("taxLine", taxLine);
      productInformation.put("taxRate", taxLine.getValue());
      productInformation.put("taxCode", taxLine.getTax().getCode());

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

    BigDecimal price = this.getExTaxUnitPrice(invoice, invoiceLine, taxLine, isPurchase);
    BigDecimal inTaxPrice = this.getInTaxUnitPrice(invoice, invoiceLine, taxLine, isPurchase);

    productInformation.put("price", price);
    productInformation.put("inTaxPrice", inTaxPrice);

    productInformation.putAll(
        this.getDiscount(invoice, invoiceLine, product.getInAti() ? inTaxPrice : price));

    productInformation.put("productName", invoiceLine.getProduct().getName());

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
            .divide(oldQty, appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN)
            .multiply(newQty)
            .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN);
    invoiceLine.setQty(qty);
    if (invoiceLine.getTypeSelect() != InvoiceLineRepository.TYPE_NORMAL
        || invoiceLine.getProduct() == null) {
      return invoiceLine;
    }

    BigDecimal exTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal taxRate = BigDecimal.ZERO;
    TaxLine taxLine = invoiceLine.getTaxLine();
    BigDecimal priceDiscounted = this.computeDiscount(invoiceLine, invoice.getInAti());
    if (taxLine != null) {
      taxRate = taxLine.getValue();
      invoiceLine.setTaxRate(taxRate);
      invoiceLine.setTaxCode(taxLine.getTax().getCode());
    }
    if (Boolean.FALSE.equals(invoice.getInAti())) {
      exTaxTotal = InvoiceLineManagement.computeAmount(qty, priceDiscounted);
      inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
    } else {
      inTaxTotal = InvoiceLineManagement.computeAmount(qty, priceDiscounted);
      exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    }
    invoiceLine.setExTaxTotal(exTaxTotal);
    invoiceLine.setCompanyExTaxTotal(this.getCompanyExTaxTotal(exTaxTotal, invoice));
    invoiceLine.setInTaxTotal(inTaxTotal);
    invoiceLine.setCompanyInTaxTotal(this.getCompanyExTaxTotal(inTaxTotal, invoice));
    invoiceLine.setPriceDiscounted(priceDiscounted);

    return this.computeAnalyticDistributionWithUpdatedQty(invoiceLine);
  }

  private InvoiceLine computeAnalyticDistributionWithUpdatedQty(InvoiceLine invoiceLine) {

    if (appAccountService.getAppAccount().getManageAnalyticAccounting()) {
      List<AnalyticMoveLine> analyticMoveLineList = this.computeAnalyticDistribution(invoiceLine);
      if (ObjectUtils.notEmpty(analyticMoveLineList)) {
        invoiceLine.setAnalyticMoveLineList(analyticMoveLineList);
      }
    }
    return invoiceLine;
  }

  @Override
  public InvoiceLine selectDefaultDistributionTemplate(InvoiceLine invoiceLine)
      throws AxelorException {

    if (invoiceLine != null && invoiceLine.getAccount() != null) {
      if (invoiceLine.getAccount().getAnalyticDistributionAuthorized()
          && invoiceLine.getAccount().getAnalyticDistributionTemplate() != null
          && accountConfigService
                  .getAccountConfig(invoiceLine.getAccount().getCompany())
                  .getAnalyticDistributionTypeSelect()
              == AccountConfigRepository.DISTRIBUTION_TYPE_PRODUCT) {

        invoiceLine.setAnalyticDistributionTemplate(
            invoiceLine.getAccount().getAnalyticDistributionTemplate());
      }
    } else {
      invoiceLine.setAnalyticDistributionTemplate(null);
    }

    return invoiceLine;
  }

  public List<InvoiceLine> updateLinesAfterFiscalPositionChange(Invoice invoice)
      throws AxelorException {
    List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
    if (CollectionUtils.isEmpty(invoiceLineList)) {
      return null;
    } else {
      for (InvoiceLine invoiceLine : invoiceLineList) {

        FiscalPosition fiscalPosition = invoice.getFiscalPosition();
        boolean isPurchase = InvoiceToolService.isPurchase(invoice);
        TaxLine taxLine = this.getTaxLine(invoice, invoiceLine, isPurchase);
        invoiceLine.setTaxLine(taxLine);
        invoiceLine.setTaxRate(taxLine.getValue());
        invoiceLine.setTaxCode(taxLine.getTax().getCode());

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
        invoiceLine.setInTaxTotal(
            invoiceLine
                .getExTaxTotal()
                .multiply(invoiceLine.getTaxRate())
                .setScale(2, RoundingMode.HALF_UP));
        invoiceLine.setCompanyInTaxTotal(invoiceLine.getInTaxTotal());
      }
    }
    return invoiceLineList;
  }
}
