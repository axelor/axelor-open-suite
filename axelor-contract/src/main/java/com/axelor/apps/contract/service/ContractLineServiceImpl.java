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
package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Duration;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

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
  protected CurrencyScaleServiceContract currencyScaleServiceContract;

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
      CurrencyScaleServiceContract currencyScaleServiceContract) {
    this.appBaseService = appBaseService;
    this.accountManagementService = accountManagementService;
    this.currencyService = currencyService;
    this.productCompanyService = productCompanyService;
    this.priceListService = priceListService;
    this.contractVersionRepo = contractVersionRepo;
    this.durationService = durationService;
    this.analyticLineModelService = analyticLineModelService;
    this.appAccountService = appAccountService;
    this.currencyScaleServiceContract = currencyScaleServiceContract;
  }

  @Override
  public Map<String, Object> reset(ContractLine contractLine) {
    if (contractLine == null) {
      return (Map<String, Object>) new ContractLine();
    }

    Map<String, Object> contractLineMap = Mapper.toMap(new ContractLine());
    contractLineMap.put("inTaxTotal", null);
    contractLineMap.put("taxLine", null);
    contractLineMap.put("productName", null);
    contractLineMap.put("unit", null);
    contractLineMap.put("price", null);
    contractLineMap.put("exTaxTotal", null);
    contractLineMap.put("description", null);
    return contractLineMap;
  }

  @Override
  public ContractLine fill(ContractLine contractLine, Product product) throws AxelorException {
    Company company =
        contractLine.getContractVersion() != null
            ? contractLine.getContractVersion().getContract() != null
                ? contractLine.getContractVersion().getContract().getCompany()
                : null
            : null;
    contractLine.setProductName((String) productCompanyService.get(product, "name", company));
    Unit unit = (Unit) productCompanyService.get(product, "salesUnit", company);
    if (unit != null) {
      contractLine.setUnit(unit);
    } else {
      contractLine.setUnit((Unit) productCompanyService.get(product, "unit", company));
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
    Preconditions.checkNotNull(
        product, "Product can't be " + "empty for compute contract line price.");

    // TODO: maybe put tax computing in another method
    contractLine.setFiscalPosition(contract.getPartner().getFiscalPosition());

    TaxLine taxLine =
        accountManagementService.getTaxLine(
            appBaseService.getTodayDate(contract.getCompany()),
            product,
            contract.getCompany(),
            contractLine.getFiscalPosition(),
            false);
    contractLine.setTaxLine(taxLine);

    if (taxLine != null
        && (Boolean) productCompanyService.get(product, "inAti", contract.getCompany())) {
      BigDecimal price = contractLine.getPrice();
      price =
          price.divide(
              taxLine.getValue().divide(new BigDecimal(100)).add(BigDecimal.ONE),
              2,
              BigDecimal.ROUND_HALF_UP);
      contractLine.setPrice(price);
    }

    BigDecimal price = contractLine.getPrice();
    BigDecimal convert =
        currencyService.getCurrencyConversionRate(
            (Currency) productCompanyService.get(product, "saleCurrency", contract.getCompany()),
            contract.getCurrency(),
            appBaseService.getTodayDate(contract.getCompany()));
    contractLine.setPrice(price.multiply(convert));

    return contractLine;
  }

  @Override
  public ContractLine fillAndCompute(ContractLine contractLine, Contract contract, Product product)
      throws AxelorException {
    if (product != null) {
      contractLine = fill(contractLine, product);
      contractLine = compute(contractLine, contract, product);
    }
    computeAnalytic(contract, contractLine);
    return contractLine;
  }

  @Override
  public ContractLine computeTotal(ContractLine contractLine, Contract contract)
      throws AxelorException {
    BigDecimal taxRate = BigDecimal.ZERO;

    if (contractLine.getTaxLine() != null) {
      taxRate = contractLine.getTaxLine().getValue().divide(new BigDecimal(100));
    }

    if (contractLine.getContractVersion() != null) {
      contractLine = computePricesPerYear(contractLine, contractLine.getContractVersion());
      contract = contractLine.getContractVersion().getContract();
    }

    BigDecimal price =
        priceListService.computeDiscount(
            contractLine.getPrice(),
            contractLine.getDiscountTypeSelect(),
            contractLine.getDiscountAmount());
    contractLine.setPriceDiscounted(price);
    BigDecimal exTaxTotal =
        currencyScaleServiceContract.getScaledValue(
            contract, contractLine.getQty().multiply(price));
    contractLine.setExTaxTotal(exTaxTotal);
    BigDecimal inTaxTotal =
        currencyScaleServiceContract.getScaledValue(
            contract, exTaxTotal.add(exTaxTotal.multiply(taxRate)));
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
            currencyScaleServiceContract.getScaledValue(
                contractLine, initialUnitPrice.multiply(qty).multiply(ratio)));
      }
      if (exTaxTotal != null) {
        contractLine.setYearlyPriceRevalued(
            currencyScaleServiceContract.getScaledValue(contractLine, exTaxTotal.multiply(ratio)));
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
}
