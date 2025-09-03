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

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.analytic.AnalyticAxisService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticLineModelServiceImpl implements AnalyticLineModelService {

  protected AppBaseService appBaseService;
  protected AppAccountService appAccountService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected AccountManagementAccountService accountManagementAccountService;
  protected AnalyticToolService analyticToolService;
  protected SaleConfigService saleConfigService;
  protected PurchaseConfigService purchaseConfigService;
  protected CurrencyScaleService currencyScaleService;
  protected AnalyticAxisService analyticAxisService;

  @Inject
  public AnalyticLineModelServiceImpl(
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      AnalyticToolService analyticToolService,
      SaleConfigService saleConfigService,
      PurchaseConfigService purchaseConfigService,
      CurrencyScaleService currencyScaleService,
      AnalyticAxisService analyticAxisService) {
    this.appBaseService = appBaseService;
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.analyticToolService = analyticToolService;
    this.saleConfigService = saleConfigService;
    this.purchaseConfigService = purchaseConfigService;
    this.currencyScaleService = currencyScaleService;
    this.analyticAxisService = analyticAxisService;
  }

  @Override
  public boolean analyzeAnalyticLineModel(AnalyticLineModel analyticLineModel, Company company)
      throws AxelorException {
    if (!analyticToolService.isManageAnalytic(company) || analyticLineModel == null) {
      return false;
    }

    if (analyticLineModel.getAnalyticMoveLineList() == null) {
      analyticLineModel.setAnalyticMoveLineList(new ArrayList<>());
    } else {
      analyticLineModel.getAnalyticMoveLineList().clear();
    }

    for (AnalyticAccount axisAnalyticAccount : this.getAxisAnalyticAccountList(analyticLineModel)) {
      AnalyticMoveLine analyticMoveLine =
          this.computeAnalyticMoveLine(analyticLineModel, company, axisAnalyticAccount);

      analyticLineModel.addAnalyticMoveLineListItem(analyticMoveLine);
    }

    return true;
  }

  protected List<AnalyticAccount> getAxisAnalyticAccountList(AnalyticLineModel analyticLineModel) {
    return Stream.of(
            analyticLineModel.getAxis1AnalyticAccount(),
            analyticLineModel.getAxis2AnalyticAccount(),
            analyticLineModel.getAxis3AnalyticAccount(),
            analyticLineModel.getAxis4AnalyticAccount(),
            analyticLineModel.getAxis5AnalyticAccount())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  protected AnalyticMoveLine computeAnalyticMoveLine(
      AnalyticLineModel analyticLineModel, Company company, AnalyticAccount analyticAccount)
      throws AxelorException {

    AnalyticMoveLine analyticMoveLine =
        analyticMoveLineService.computeAnalytic(company, analyticAccount);
    analyticMoveLineService.setAnalyticCurrency(company, analyticMoveLine);

    analyticMoveLine.setDate(appBaseService.getTodayDate(company));
    analyticMoveLine.setAmount(
        currencyScaleService.getScaledValue(
            analyticMoveLine, analyticLineModel.getCompanyExTaxTotal()));
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);

    return analyticMoveLine;
  }

  public AnalyticLineModel getAndComputeAnalyticDistribution(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    if (!productAccountManageAnalytic(analyticLineModel)) {
      return analyticLineModel;
    }

    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticMoveLineService.getAnalyticDistributionTemplate(
            analyticLineModel.getPartner(),
            analyticLineModel.getProduct(),
            analyticLineModel.getCompany(),
            analyticLineModel.getTradingName(),
            analyticLineModel.getAccount(),
            analyticLineModel.getIsPurchase());

    analyticLineModel.setAnalyticDistributionTemplate(analyticDistributionTemplate);

    if (analyticLineModel.getAnalyticMoveLineList() != null) {
      analyticLineModel.getAnalyticMoveLineList().clear();
    }

    this.computeAnalyticDistribution(analyticLineModel);

    analyticLineModel.copyToModel();

    return analyticLineModel;
  }

  @Override
  public boolean isFreeAnalyticDistribution(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    return analyticToolService.isFreeAnalyticDistribution(analyticLineModel.getCompany());
  }

  @Override
  public boolean productAccountManageAnalytic(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    Product product = analyticLineModel.getProduct();
    return analyticToolService.isManageAnalytic(analyticLineModel.getCompany())
        && product != null
        && accountManagementAccountService
            .getProductAccount(
                product,
                analyticLineModel.getCompany(),
                analyticLineModel.getFiscalPosition(),
                analyticLineModel.getIsPurchase(),
                false)
            .getAnalyticDistributionAuthorized();
  }

  @Override
  public AnalyticLineModel computeAnalyticDistribution(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    List<AnalyticMoveLine> analyticMoveLineList = analyticLineModel.getAnalyticMoveLineList();

    if (CollectionUtils.isEmpty(analyticMoveLineList)) {
      this.createAnalyticDistributionWithTemplate(analyticLineModel);
    }

    if (analyticMoveLineList != null) {
      LocalDate date = appAccountService.getTodayDate(this.getCompany(analyticLineModel));

      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLineService.updateAnalyticMoveLine(
            analyticMoveLine,
            currencyScaleService.getScaledValue(
                analyticMoveLine, analyticLineModel.getCompanyExTaxTotal()),
            date);
      }
    }

    analyticLineModel.copyToModel();

    return analyticLineModel;
  }

  @Override
  public AnalyticLineModel createAnalyticDistributionWithTemplate(
      AnalyticLineModel analyticLineModel) throws AxelorException {
    this.clearAnalyticInLine(analyticLineModel);

    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            analyticLineModel.getAnalyticDistributionTemplate(),
            currencyScaleService.getCompanyScaledValue(
                analyticLineModel.getCompany(), analyticLineModel.getCompanyExTaxTotal()),
            AnalyticMoveLineRepository.STATUS_FORECAST_ORDER,
            appBaseService.getTodayDate(this.getCompany(analyticLineModel)));

    analyticLineModel.clearAnalyticMoveLineList();
    if (ObjectUtils.notEmpty(analyticMoveLineList)) {
      analyticMoveLineList.forEach(analyticLineModel::addAnalyticMoveLineListItem);
    }

    return analyticLineModel;
  }

  protected Company getCompany(AnalyticLineModel analyticLineModel) {
    return analyticLineModel.getCompany() != null
        ? analyticLineModel.getCompany()
        : Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
  }

  protected void clearAnalyticInLine(AnalyticLineModel analyticLineModel) {
    analyticLineModel.setAxis1AnalyticAccount(null);
    analyticLineModel.setAxis2AnalyticAccount(null);
    analyticLineModel.setAxis3AnalyticAccount(null);
    analyticLineModel.setAxis4AnalyticAccount(null);
    analyticLineModel.setAxis5AnalyticAccount(null);
  }

  @Override
  public void setInvoiceLineAnalyticInfo(
      AnalyticLineModel analyticLineModel, InvoiceLine invoiceLine) {
    invoiceLine.setAnalyticDistributionTemplate(
        analyticLineModel.getAnalyticDistributionTemplate());

    invoiceLine.setAxis1AnalyticAccount(analyticLineModel.getAxis1AnalyticAccount());
    invoiceLine.setAxis2AnalyticAccount(analyticLineModel.getAxis2AnalyticAccount());
    invoiceLine.setAxis3AnalyticAccount(analyticLineModel.getAxis3AnalyticAccount());
    invoiceLine.setAxis4AnalyticAccount(analyticLineModel.getAxis4AnalyticAccount());
    invoiceLine.setAxis5AnalyticAccount(analyticLineModel.getAxis5AnalyticAccount());
  }

  @Override
  public boolean analyticDistributionTemplateRequired(boolean isPurchase, Company company)
      throws AxelorException {
    return analyticToolService.isManageAnalytic(company)
        && ((isPurchase
                && purchaseConfigService
                    .getPurchaseConfig(company)
                    .getIsAnalyticDistributionRequired())
            || (!isPurchase
                && saleConfigService.getSaleConfig(company).getIsAnalyticDistributionRequired()));
  }

  @Override
  public void checkRequiredAxisByCompany(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    if (!CollectionUtils.isEmpty(analyticLineModel.getAnalyticMoveLineList())) {
      Company company = analyticLineModel.getCompany();
      List<AnalyticAxis> analyticAxisList =
          analyticLineModel.getAnalyticMoveLineList().stream()
              .map(AnalyticMoveLine::getAnalyticAxis)
              .collect(Collectors.toList());
      analyticAxisService.checkRequiredAxisByCompany(company, analyticAxisList);
    }
  }
}
