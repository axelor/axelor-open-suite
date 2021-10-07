/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
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
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.tool.service.ListToolService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
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
  protected AnalyticAccountRepository analyticAccountRepository;
  protected AccountAnalyticRulesRepository accountAnalyticRulesRepository;
  protected ListToolService listToolService;

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
      AccountConfigService accountConfigService,
      AnalyticAccountRepository analyticAccountRepository,
      AccountAnalyticRulesRepository accountAnalyticRulesRepository,
      ListToolService listToolService) {

    this.accountManagementAccountService = accountManagementAccountService;
    this.currencyService = currencyService;
    this.priceListService = priceListService;
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.productCompanyService = productCompanyService;
    this.invoiceLineRepo = invoiceLineRepo;
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.analyticAccountRepository = analyticAccountRepository;
    this.accountAnalyticRulesRepository = accountAnalyticRulesRepository;
    this.listToolService = listToolService;
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

    List<AnalyticMoveLine> AnalyticMoveLineList = invoiceLine.getAnalyticMoveLineList();

    if ((AnalyticMoveLineList == null || AnalyticMoveLineList.isEmpty())) {
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
        for (AnalyticMoveLine AnalyticMoveLine : AnalyticMoveLineList) {
          analyticMoveLineService.updateAnalyticMoveLine(
              AnalyticMoveLine, invoiceLine.getCompanyExTaxTotal(), date);
        }
      }
      return AnalyticMoveLineList;
    }
  }

  @Override
  public List<AnalyticMoveLine> createAnalyticDistributionWithTemplate(InvoiceLine invoiceLine) {
    List<AnalyticMoveLine> AnalyticMoveLineList =
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

    return AnalyticMoveLineList;
  }

  @Override
  public TaxLine getTaxLine(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase)
      throws AxelorException {

    return accountManagementAccountService.getTaxLine(
        appAccountService.getTodayDate(invoice.getCompany()),
        invoiceLine.getProduct(),
        invoice.getCompany(),
        invoice.getPartner().getFiscalPosition(),
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
      productInformation.put("AnalyticMoveLineList", null);
    }
    return productInformation;
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
    FiscalPosition fiscalPosition = invoice.getPartner().getFiscalPosition();
    try {
      taxLine = this.getTaxLine(invoice, invoiceLine, isPurchase);
      invoiceLine.setTaxLine(taxLine);
      productInformation.put("taxLine", taxLine);
      productInformation.put("taxRate", taxLine.getValue());
      productInformation.put("taxCode", taxLine.getTax().getCode());
      productInformation.put("fixedAssets", product.getFixedAssets());

      Tax tax = accountManagementAccountService.getProductTax(product, company, null, isPurchase);
      TaxEquiv taxEquiv = Beans.get(FiscalPositionService.class).getTaxEquiv(fiscalPosition, tax);
      productInformation.put("taxEquiv", taxEquiv);

      Account account =
          accountManagementAccountService.getProductAccount(
              product, company, fiscalPosition, isPurchase, product.getFixedAssets());
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
      List<AnalyticMoveLine> AnalyticMoveLineList = this.computeAnalyticDistribution(invoiceLine);
      if (ObjectUtils.notEmpty(AnalyticMoveLineList)) {
        invoiceLine.setAnalyticMoveLineList(AnalyticMoveLineList);
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

  @Override
  public boolean compareNbrOfAnalyticAxisSelect(InvoiceLine invoiceLine, int position)
      throws AxelorException {
    return invoiceLine != null
        && invoiceLine.getInvoice() != null
        && invoiceLine.getInvoice().getCompany() != null
        && position
            <= accountConfigService
                .getAccountConfig(invoiceLine.getInvoice().getCompany())
                .getNbrOfAnalyticAxisSelect();
  }

  public List<Long> setAxisDomains(InvoiceLine invoiceLine, int position) throws AxelorException {
    List<Long> analyticAccountListByAxis = new ArrayList<Long>();
    List<Long> analyticAccountListByRules = new ArrayList<Long>();

    AnalyticAxis analyticAxis = new AnalyticAxis();

    if (compareNbrOfAnalyticAxisSelect(invoiceLine, position)) {

      for (AnalyticAxisByCompany axis :
          accountConfigService
              .getAccountConfig(invoiceLine.getInvoice().getCompany())
              .getAnalyticAxisByCompanyList()) {
        if (axis.getOrderSelect() == position) {
          analyticAxis = axis.getAnalyticAxis();
        }
      }

      for (AnalyticAccount analyticAccount :
          analyticAccountRepository.findByAnalyticAxis(analyticAxis).fetch()) {
        analyticAccountListByAxis.add(analyticAccount.getId());
      }
      if (invoiceLine.getAccount() != null) {
        List<AnalyticAccount> analyticAccountList =
            accountAnalyticRulesRepository.findAnalyticAccountByAccounts(invoiceLine.getAccount());
        if (!analyticAccountList.isEmpty()) {
          for (AnalyticAccount analyticAccount : analyticAccountList) {
            analyticAccountListByRules.add(analyticAccount.getId());
          }
          analyticAccountListByAxis =
              listToolService.intersection(analyticAccountListByAxis, analyticAccountListByRules);
        }
      }
    }
    return analyticAccountListByAxis;
  }

  @Override
  public InvoiceLine analyzeInvoiceLine(InvoiceLine invoiceLine) throws AxelorException {
    if (invoiceLine != null) {

      if (invoiceLine.getAnalyticMoveLineList() == null) {
        invoiceLine.setAnalyticMoveLineList(new ArrayList<>());
      } else {
        invoiceLine
            .getAnalyticMoveLineList()
            .forEach(analyticMoveLine -> analyticMoveLine.setInvoiceLine(null));
        invoiceLine.getAnalyticMoveLineList().clear();
      }

      AnalyticMoveLine analyticMoveLine = null;

      if (invoiceLine.getAxis1AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                invoiceLine, invoiceLine.getAxis1AnalyticAccount());
        invoiceLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (invoiceLine.getAxis2AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                invoiceLine, invoiceLine.getAxis2AnalyticAccount());
        invoiceLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (invoiceLine.getAxis3AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                invoiceLine, invoiceLine.getAxis3AnalyticAccount());
        invoiceLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (invoiceLine.getAxis4AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                invoiceLine, invoiceLine.getAxis4AnalyticAccount());
        invoiceLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (invoiceLine.getAxis5AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                invoiceLine, invoiceLine.getAxis5AnalyticAccount());
        invoiceLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
    }
    return invoiceLine;
  }

  @Override
  public InvoiceLine removeAnalyticOnRemoveProduct(InvoiceLine invoiceLine) {
    if (invoiceLine != null && invoiceLine.getProduct() == null) {
      invoiceLine = removeAnalytic(invoiceLine);
    }
    return invoiceLine;
  }

  @Override
  public InvoiceLine removeAnalyticOnRemoveAccount(InvoiceLine invoiceLine) {
    if (invoiceLine != null && invoiceLine.getAccount() == null) {
      invoiceLine = removeAnalytic(invoiceLine);
    }
    return invoiceLine;
  }

  public InvoiceLine removeAnalytic(InvoiceLine invoiceLine) {
    invoiceLine.setAnalyticDistributionTemplate(null);
    return clearAnalyticAccounting(invoiceLine);
  }

  @Override
  public InvoiceLine clearAnalyticAccounting(InvoiceLine invoiceLine) {
    invoiceLine.setAxis1AnalyticAccount(null);
    invoiceLine.setAxis2AnalyticAccount(null);
    invoiceLine.setAxis3AnalyticAccount(null);
    invoiceLine.setAxis4AnalyticAccount(null);
    invoiceLine.setAxis5AnalyticAccount(null);
    invoiceLine
        .getAnalyticMoveLineList()
        .forEach(analyticMoveLine -> analyticMoveLine.setInvoiceLine(null));
    invoiceLine.getAnalyticMoveLineList().clear();
    return invoiceLine;
  }

  @Override
  public InvoiceLine printAnalyticAccount(InvoiceLine invoiceLine) throws AxelorException {
    if (invoiceLine.getAnalyticMoveLineList() != null
        && !invoiceLine.getAnalyticMoveLineList().isEmpty()
        && invoiceLine.getInvoice() != null
        && invoiceLine.getInvoice().getCompany() != null) {
      List<AnalyticMoveLine> analyticMoveLineList = new ArrayList();
      for (AnalyticAxisByCompany analyticAxisByCompany :
          accountConfigService
              .getAccountConfig(invoiceLine.getInvoice().getCompany())
              .getAnalyticAxisByCompanyList()) {
        for (AnalyticMoveLine analyticMoveLine : invoiceLine.getAnalyticMoveLineList()) {
          if (analyticMoveLine.getAnalyticAxis() == analyticAxisByCompany.getAnalyticAxis()) {
            analyticMoveLineList.add(analyticMoveLine);
          }
        }
        if (analyticMoveLineList.size() == 1
            && analyticMoveLineList.get(0).getPercentage().compareTo(new BigDecimal(100)) == 0) {
          switch (analyticAxisByCompany.getOrderSelect()) {
            case 1:
              invoiceLine.setAxis1AnalyticAccount(analyticMoveLineList.get(0).getAnalyticAccount());
              break;
            case 2:
              invoiceLine.setAxis2AnalyticAccount(analyticMoveLineList.get(0).getAnalyticAccount());
              break;
            case 3:
              invoiceLine.setAxis3AnalyticAccount(analyticMoveLineList.get(0).getAnalyticAccount());
              break;
            case 4:
              invoiceLine.setAxis4AnalyticAccount(analyticMoveLineList.get(0).getAnalyticAccount());
              break;
            case 5:
              invoiceLine.setAxis5AnalyticAccount(analyticMoveLineList.get(0).getAnalyticAccount());
              break;
            default:
              break;
          }
        }
        analyticMoveLineList.clear();
      }
    }
    return invoiceLine;
  }

  public boolean checkAxisAccount(InvoiceLine invoiceLine, AnalyticAxis analyticAxis) {
    BigDecimal sum = BigDecimal.ZERO;
    for (AnalyticMoveLine analyticMoveLine : invoiceLine.getAnalyticMoveLineList()) {
      if (analyticMoveLine.getAnalyticAxis() == analyticAxis) {
        sum = sum.add(analyticMoveLine.getPercentage());
      }
    }

    if (sum.compareTo(new BigDecimal(100)) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public InvoiceLine checkAnalyticMoveLineForAxis(InvoiceLine invoiceLine) {
    if (invoiceLine.getAxis1AnalyticAccount() != null) {
      if (!checkAxisAccount(invoiceLine, invoiceLine.getAxis1AnalyticAccount().getAnalyticAxis())) {
        invoiceLine.setAxis1AnalyticAccount(null);
      }
    }
    if (invoiceLine.getAxis2AnalyticAccount() != null) {
      if (!checkAxisAccount(invoiceLine, invoiceLine.getAxis2AnalyticAccount().getAnalyticAxis())) {
        invoiceLine.setAxis2AnalyticAccount(null);
      }
    }
    if (invoiceLine.getAxis3AnalyticAccount() != null) {
      if (!checkAxisAccount(invoiceLine, invoiceLine.getAxis3AnalyticAccount().getAnalyticAxis())) {
        invoiceLine.setAxis3AnalyticAccount(null);
      }
    }
    if (invoiceLine.getAxis4AnalyticAccount() != null) {
      if (!checkAxisAccount(invoiceLine, invoiceLine.getAxis4AnalyticAccount().getAnalyticAxis())) {
        invoiceLine.setAxis4AnalyticAccount(null);
      }
    }
    if (invoiceLine.getAxis5AnalyticAccount() != null) {
      if (!checkAxisAccount(invoiceLine, invoiceLine.getAxis5AnalyticAccount().getAnalyticAxis())) {
        invoiceLine.setAxis5AnalyticAccount(null);
      }
    }
    return invoiceLine;
  }
}
