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
package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ContractLineServiceImpl implements ContractLineService {
  protected AppBaseService appBaseService;
  protected AccountManagementService accountManagementService;
  protected CurrencyService currencyService;
  protected ProductCompanyService productCompanyService;
  protected ContractVersionRepository contractVersionRepo;
  protected PriceListService priceListService;
  protected DurationService durationService;
  protected AnalyticLineModelService analyticLineModelService;
  protected AppAccountService appAccountService;
  protected CurrencyScaleService currencyScaleService;
  protected TaxService taxService;
  protected AppSaleService appSaleService;

  @Inject
  public ContractLineServiceImpl(
      AppBaseService appBaseService,
      AccountManagementService accountManagementService,
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      PriceListService priceListService,
      ContractVersionRepository contractVersionRepo,
      DurationService durationService,
      AnalyticLineModelService analyticLineModelService,
      AppAccountService appAccountService,
      CurrencyScaleService currencyScaleService,
      TaxService taxService,
      AppSaleService appSaleService) {
    this.appBaseService = appBaseService;
    this.accountManagementService = accountManagementService;
    this.currencyService = currencyService;
    this.productCompanyService = productCompanyService;
    this.priceListService = priceListService;
    this.contractVersionRepo = contractVersionRepo;
    this.durationService = durationService;
    this.analyticLineModelService = analyticLineModelService;
    this.appAccountService = appAccountService;
    this.currencyScaleService = currencyScaleService;
    this.taxService = taxService;
    this.appSaleService = appSaleService;
  }

  @Override
  public Map<String, Object> reset(ContractLine contractLine) {
    if (contractLine == null) {
      return (Map<String, Object>) new ContractLine();
    }

    Map<String, Object> contractLineMap = Mapper.toMap(new ContractLine());
    contractLineMap.put("inTaxTotal", null);
    contractLineMap.put("taxLineSet", null);
    contractLineMap.put("productName", null);
    contractLineMap.put("unit", null);
    contractLineMap.put("price", null);
    contractLineMap.put("exTaxTotal", null);
    contractLineMap.put("description", null);
    return contractLineMap;
  }

  public ContractLine fill(ContractLine contractLine, Contract contract, Product product)
      throws AxelorException {
    Company company = contract != null ? contract.getCompany() : null;
    contractLine.setProductName((String) productCompanyService.get(product, "name", company));
    Unit unit = (Unit) productCompanyService.get(product, "salesUnit", company);
    if (unit != null) {
      contractLine.setUnit(unit);
    } else {
      contractLine.setUnit((Unit) productCompanyService.get(product, "unit", company));
    }

    Map<String, Object> discounts =
        getDiscountsFromPriceLists(contract, contractLine, contractLine.getPrice());

    if (discounts != null) {
      contractLine.setDiscountAmount((BigDecimal) discounts.get("discountAmount"));
      contractLine.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
    }
    contractLine.setPrice((BigDecimal) productCompanyService.get(product, "salePrice", company));
    contractLine.setDescription(
        (String) productCompanyService.get(product, "description", company));
    return contractLine;
  }

  @Override
  public ContractLine fillDefault(ContractLine contractLine, ContractVersion contractVersion) {
    contractLine.setFromDate(contractVersion.getSupposedActivationDate());
    return contractLine;
  }

  @Override
  public ContractLine compute(ContractLine contractLine, Contract contract, Product product)
      throws AxelorException {
    Preconditions.checkNotNull(
        contract, I18n.get("Contract can't be " + "empty for compute contract line price."));

    // TODO: maybe put tax computing in another method
    contractLine.setFiscalPosition(contract.getPartner().getFiscalPosition());

    Set<TaxLine> taxLineSet = getTaxLineSet(contractLine, contract, product);
    contractLine.setTaxLineSet(taxLineSet);

    setPrice(contractLine, contract, product, taxLineSet);
    setPriceScale(contractLine, contract, product);

    return contractLine;
  }

  protected void setPrice(
      ContractLine contractLine, Contract contract, Product product, Set<TaxLine> taxLineSet)
      throws AxelorException {
    BigDecimal price = contractLine.getPrice();
    if (product != null
        && CollectionUtils.isNotEmpty(taxLineSet)
        && (Boolean) productCompanyService.get(product, "inAti", contract.getCompany())) {
      price =
          price.divide(
              taxService.getTotalTaxRate(taxLineSet).add(BigDecimal.ONE),
              2,
              BigDecimal.ROUND_HALF_UP);
    }
    contractLine.setPrice(price);
  }

  protected void setPriceScale(ContractLine contractLine, Contract contract, Product product)
      throws AxelorException {
    BigDecimal price = contractLine.getPrice();
    if (product != null) {
      BigDecimal convert =
          currencyService.getCurrencyConversionRate(
              (Currency) productCompanyService.get(product, "saleCurrency", contract.getCompany()),
              contract.getCurrency(),
              appBaseService.getTodayDate(contract.getCompany()));
      contractLine.setPrice(
          price
              .multiply(convert)
              .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
    }
  }

  protected Set<TaxLine> getTaxLineSet(
      ContractLine contractLine, Contract contract, Product product) throws AxelorException {
    Set<TaxLine> taxLineSet = Set.of();
    int targetTypeSelect = contract.getTargetTypeSelect();
    Set<TaxLine> contractTaxLineSet = contractLine.getTaxLineSet();

    if (CollectionUtils.isNotEmpty(contractTaxLineSet)) {
      return contractTaxLineSet;
    }

    if (product != null
        && (targetTypeSelect == ContractRepository.CUSTOMER_CONTRACT
            || targetTypeSelect == ContractRepository.SUPPLIER_CONTRACT)) {
      taxLineSet =
          accountManagementService.getTaxLineSet(
              appBaseService.getTodayDate(contract.getCompany()),
              product,
              contract.getCompany(),
              contractLine.getFiscalPosition(),
              contract.getTargetTypeSelect() == ContractRepository.SUPPLIER_CONTRACT);
    }

    return taxLineSet;
  }

  @Override
  public ContractLine fillAndCompute(ContractLine contractLine, Contract contract, Product product)
      throws AxelorException {
    if (product != null) {
      contractLine = fill(contractLine, contract, product);
      contractLine = compute(contractLine, contract, product);
    }
    computeAnalytic(contract, contractLine);
    return contractLine;
  }

  @Override
  public ContractLine computeTotal(ContractLine contractLine, Contract contract)
      throws AxelorException {
    BigDecimal taxRate = BigDecimal.ZERO;

    Set<TaxLine> taxLineSet = contractLine.getTaxLineSet();
    if (CollectionUtils.isNotEmpty(taxLineSet)) {
      taxRate = taxService.getTotalTaxRate(taxLineSet);
    }

    if (contractLine.getContractVersion() != null) {
      contractLine = computePricesPerYear(contractLine, contractLine.getContractVersion());
    }

    Map<String, Object> discounts =
        getDiscountsFromPriceLists(contract, contractLine, contractLine.getPrice());
    BigDecimal price =
        Optional.ofNullable(discounts)
            .map(d -> (BigDecimal) d.get("price"))
            .orElse(contractLine.getPrice());
    Integer discountTypeSelect =
        Optional.ofNullable(discounts)
            .map(d -> (Integer) d.get("discountTypeSelect"))
            .orElse(contractLine.getDiscountTypeSelect());
    BigDecimal discountAmount =
        Optional.ofNullable(discounts)
            .map(d -> (BigDecimal) d.get("discountAmount"))
            .orElse(contractLine.getDiscountAmount());

    price = priceListService.computeDiscount(price, discountTypeSelect, discountAmount);

    contractLine.setPriceDiscounted(price);
    contractLine.setDiscountTypeSelect(discountTypeSelect);
    contractLine.setDiscountAmount(discountAmount);
    BigDecimal exTaxTotal =
        currencyScaleService.getScaledValue(contract, contractLine.getQty().multiply(price));
    contractLine.setExTaxTotal(exTaxTotal);
    BigDecimal inTaxTotal =
        currencyScaleService.getScaledValue(contract, exTaxTotal.add(exTaxTotal.multiply(taxRate)));
    contractLine.setInTaxTotal(inTaxTotal);

    return contractLine;
  }

  @Override
  @Transactional
  public void updateContractLinesFromContractVersion(ContractVersion contractVersion) {
    for (ContractLine line : contractVersion.getContractLineList()) {
      if (line.getFromDate() == null) {
        line.setFromDate(contractVersion.getSupposedActivationDate());
      }
    }
    contractVersionRepo.save(contractVersion);
  }

  @Override
  public ContractLine computePricesPerYear(
      ContractLine contractLine, ContractVersion contractVersion) throws AxelorException {
    Duration duration = contractVersion.getInvoicingDuration();

    if (duration != null) {
      BigDecimal initialUnitPrice = contractLine.getInitialUnitPrice();
      BigDecimal qty = contractLine.getQty();
      BigDecimal exTaxTotal = contractLine.getExTaxTotal();
      int durationType = duration.getTypeSelect();
      Integer durationValue = duration.getValue();
      BigDecimal ratio =
          durationService
              .getFactor(durationType)
              .divide(
                  BigDecimal.valueOf(durationValue),
                  AppBaseService.COMPUTATION_SCALING,
                  RoundingMode.HALF_UP);
      if (initialUnitPrice != null && qty != null) {
        contractLine.setInitialPricePerYear(
            currencyScaleService.getScaledValue(
                contractLine, initialUnitPrice.multiply(qty).multiply(ratio)));
      }
      if (exTaxTotal != null) {
        contractLine.setYearlyPriceRevalued(
            currencyScaleService.getScaledValue(contractLine, exTaxTotal.multiply(ratio)));
      }
    }

    return contractLine;
  }

  public void computeAnalytic(Contract contract, ContractLine contractLine) throws AxelorException {
    if (appAccountService.isApp("supplychain")) {
      AnalyticLineModel analyticLineModel =
          new AnalyticLineContractModel(contractLine, null, contract);
      analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);
    }
  }

  @Override
  public ContractLine resetProductInformation(ContractLine contractLine) {
    contractLine.setProductName(null);
    contractLine.setQty(null);
    contractLine.setPrice(null);
    contractLine.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_NONE);
    contractLine.setDiscountAmount(null);
    contractLine.setTaxLineSet(Sets.newHashSet());
    contractLine.setUnit(null);
    contractLine.setExTaxTotal(null);
    contractLine.setInTaxTotal(null);
    contractLine.setDescription(null);

    return contractLine;
  }

  @Override
  public Map<String, Object> getDiscountsFromPriceLists(
      Contract contract, ContractLine contractLine, BigDecimal price) {

    Map<String, Object> discounts = null;

    if (contract != null) {
      PriceList priceList = contract.getPriceList();

      if (priceList != null) {
        PriceListLine priceListLine = this.getPriceListLine(contractLine, priceList, price);
        discounts = priceListService.getReplacedPriceAndDiscounts(priceList, priceListLine, price);
      }
    }

    return discounts;
  }

  @Override
  public PriceListLine getPriceListLine(
      ContractLine contractLine, PriceList priceList, BigDecimal price) {

    return priceListService.getPriceListLine(
        contractLine.getProduct(), contractLine.getQty(), priceList, price);
  }

  @Override
  public String computeProductDomain(Contract contract) {
    String domain =
        "self.isModel = false"
            + " and (self.endDate = null or self.endDate > :__date__)"
            + " and self.dtype = 'Product'";

    if (contract == null) {
      return domain;
    }
    if (appBaseService.getAppBase().getEnableTradingNamesManagement()
        && appSaleService.getAppSale().getEnableSalesProductByTradName()
        && contract.getTradingName() != null
        && contract.getCompany() != null
        && !CollectionUtils.isEmpty(contract.getCompany().getTradingNameList())) {
      domain +=
          " AND " + contract.getTradingName().getId() + " member of self.tradingNameSellerSet";
    }

    int targetTypeSelect = contract.getTargetTypeSelect();
    if (targetTypeSelect == ContractRepository.CUSTOMER_CONTRACT
        || targetTypeSelect == ContractRepository.YEB_CUSTOMER_CONTRACT) {
      domain += " AND self.sellable = true";
    } else if (targetTypeSelect == ContractRepository.SUPPLIER_CONTRACT
        || targetTypeSelect == ContractRepository.YEB_SUPPLIER_CONTRACT) {
      domain += " AND self.purchasable = true";
    }

    return domain;
  }

  @Override
  public void checkAnalyticAxisByCompany(Contract contract) throws AxelorException {
    if (contract == null || contract.getCurrentContractVersion() == null) {
      return;
    }

    if (!ObjectUtils.isEmpty(contract.getCurrentContractVersion().getContractLineList())) {
      for (ContractLine contractLine : contract.getCurrentContractVersion().getContractLineList()) {
        AnalyticLineContractModel analyticLineModel =
            new AnalyticLineContractModel(
                contractLine, contract.getCurrentContractVersion(), contract);
        analyticLineModelService.checkRequiredAxisByCompany(analyticLineModel);
      }
    }
  }
}
