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

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.exception.ContractExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.service.ListToolService;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;

public class ContractLineServiceImpl implements ContractLineService {
  protected AppBaseService appBaseService;
  protected AppAccountService appAccountService;
  protected AccountManagementService accountManagementService;
  protected AccountConfigService accountConfigService;
  protected CurrencyService currencyService;
  protected ProductCompanyService productCompanyService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected AnalyticToolService analyticToolService;
  protected ListToolService listToolService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected AnalyticAccountRepository analyticAccountRepo;
  protected AccountAnalyticRulesRepository accountAnalyticRulesRepo;

  @Inject
  public ContractLineServiceImpl(
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      AccountManagementService accountManagementService,
      AccountConfigService accountConfigService,
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticToolService analyticToolService,
      ListToolService listToolService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      AnalyticAccountRepository analyticAccountRepo,
      AccountAnalyticRulesRepository accountAnalyticRulesRepo) {
    this.appBaseService = appBaseService;
    this.appAccountService = appAccountService;
    this.accountManagementService = accountManagementService;
    this.accountConfigService = accountConfigService;
    this.currencyService = currencyService;
    this.productCompanyService = productCompanyService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.analyticToolService = analyticToolService;
    this.listToolService = listToolService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.analyticAccountRepo = analyticAccountRepo;
    this.accountAnalyticRulesRepo = accountAnalyticRulesRepo;
  }

  @Override
  public ContractLine reset(ContractLine contractLine) {
    if (contractLine == null) {
      return new ContractLine();
    }
    contractLine.setTaxLine(null);
    contractLine.setProductName(null);
    contractLine.setUnit(null);
    contractLine.setPrice(null);
    contractLine.setExTaxTotal(null);
    contractLine.setInTaxTotal(null);
    contractLine.setDescription(null);
    return contractLine;
  }

  @Override
  public ContractLine fill(ContractLine contractLine, Product product) throws AxelorException {
    Preconditions.checkNotNull(product, I18n.get(ContractExceptionMessage.CONTRACT_EMPTY_PRODUCT));
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
    contractLine = fill(contractLine, product);
    contractLine = compute(contractLine, contract, product);
    return contractLine;
  }

  @Override
  public ContractLine computeTotal(ContractLine contractLine) {
    BigDecimal taxRate = BigDecimal.ZERO;

    if (contractLine.getTaxLine() != null) {
      taxRate = contractLine.getTaxLine().getValue().divide(new BigDecimal(100));
    }

    BigDecimal exTaxTotal =
        contractLine.getQty().multiply(contractLine.getPrice()).setScale(2, RoundingMode.HALF_UP);
    contractLine.setExTaxTotal(exTaxTotal);
    BigDecimal inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
    contractLine.setInTaxTotal(inTaxTotal);

    return contractLine;
  }

  @Override
  public ContractLine createAnalyticDistributionWithTemplate(
      ContractLine contractLine, Contract contract) {
    this.clearAnalyticInLine(contractLine);

    List<AnalyticMoveLine> analyticMoveLineList =
        Beans.get(AnalyticMoveLineService.class)
            .generateLines(
                contractLine.getAnalyticDistributionTemplate(),
                contractLine.getExTaxTotal(),
                AnalyticMoveLineRepository.STATUS_FORECAST_CONTRACT,
                appAccountService.getTodayDate(contract.getCompany()));

    contractLine.setAnalyticMoveLineList(analyticMoveLineList);
    return contractLine;
  }

  protected void clearAnalyticInLine(ContractLine contractLine) {
    contractLine.setAxis1AnalyticAccount(null);
    contractLine.setAxis2AnalyticAccount(null);
    contractLine.setAxis3AnalyticAccount(null);
    contractLine.setAxis4AnalyticAccount(null);
    contractLine.setAxis5AnalyticAccount(null);
  }

  @Override
  public ContractLine analyzeContractLine(
      ContractLine contractLine, Contract contract, Company company) throws AxelorException {
    if (contractLine == null) {
      return null;
    }

    if (contractLine.getAnalyticMoveLineList() == null) {
      contractLine.setAnalyticMoveLineList(new ArrayList<>());
    } else {
      contractLine.getAnalyticMoveLineList().clear();
    }

    for (AnalyticAccount axisAnalyticAccount : this.getAxisAnalyticAccountList(contractLine)) {
      AnalyticMoveLine analyticMoveLine =
          this.computeAnalyticMoveLine(contractLine, contract, company, axisAnalyticAccount);

      contractLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }

    return contractLine;
  }

  protected List<AnalyticAccount> getAxisAnalyticAccountList(ContractLine contractLine) {
    return Stream.of(
            contractLine.getAxis1AnalyticAccount(),
            contractLine.getAxis2AnalyticAccount(),
            contractLine.getAxis3AnalyticAccount(),
            contractLine.getAxis4AnalyticAccount(),
            contractLine.getAxis5AnalyticAccount())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  protected AnalyticMoveLine computeAnalyticMoveLine(
      ContractLine contractLine,
      Contract contract,
      Company company,
      AnalyticAccount analyticAccount)
      throws AxelorException {
    AnalyticMoveLine analyticMoveLine =
        analyticMoveLineService.computeAnalytic(company, analyticAccount);

    analyticMoveLine.setDate(appAccountService.getTodayDate(company));
    analyticMoveLine.setAmount(contractLine.getExTaxTotal());
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_CONTRACT);

    return analyticMoveLine;
  }

  @Override
  public ContractLine printAnalyticAccount(ContractLine contractLine, Company company)
      throws AxelorException {
    if (CollectionUtils.isEmpty(contractLine.getAnalyticMoveLineList()) || company == null) {
      return contractLine;
    }

    List<AnalyticMoveLine> analyticMoveLineList;

    for (AnalyticAxisByCompany analyticAxisByCompany :
        accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList()) {
      analyticMoveLineList =
          contractLine.getAnalyticMoveLineList().stream()
              .filter(it -> it.getAnalyticAxis().equals(analyticAxisByCompany.getAnalyticAxis()))
              .filter(it -> it.getPercentage().compareTo(new BigDecimal(100)) == 0)
              .collect(Collectors.toList());

      if (analyticMoveLineList.size() == 1) {
        AnalyticMoveLine analyticMoveLine = analyticMoveLineList.get(0);
        this.setAxisAccount(contractLine, analyticAxisByCompany, analyticMoveLine);
      }
    }

    return contractLine;
  }

  protected void setAxisAccount(
      ContractLine contractLine,
      AnalyticAxisByCompany analyticAxisByCompany,
      AnalyticMoveLine analyticMoveLine) {
    AnalyticAccount analyticAccount = analyticMoveLine.getAnalyticAccount();

    switch (analyticAxisByCompany.getSequence()) {
      case 0:
        contractLine.setAxis1AnalyticAccount(analyticAccount);
        break;
      case 1:
        contractLine.setAxis2AnalyticAccount(analyticAccount);
        break;
      case 2:
        contractLine.setAxis3AnalyticAccount(analyticAccount);
        break;
      case 3:
        contractLine.setAxis4AnalyticAccount(analyticAccount);
        break;
      case 4:
        contractLine.setAxis5AnalyticAccount(analyticAccount);
        break;
      default:
        break;
    }
  }
}
