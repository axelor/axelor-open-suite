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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.model.AnalyticLineModel;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import jakarta.inject.Inject;
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
  protected CurrencyScaleService currencyScaleService;
  protected AnalyticAxisService analyticAxisService;

  @Inject
  public AnalyticLineModelServiceImpl(
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      AnalyticToolService analyticToolService,
      CurrencyScaleService currencyScaleService,
      AnalyticAxisService analyticAxisService) {
    this.appBaseService = appBaseService;
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.analyticToolService = analyticToolService;
    this.currencyScaleService = currencyScaleService;
    this.analyticAxisService = analyticAxisService;
  }

  @Override
  public boolean analyzeAnalyticLineModel(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    if (analyticLineModel == null
        || !analyticToolService.isManageAnalytic(analyticLineModel.getCompany())) {
      return false;
    }

    AnalyticLine analyticLine = analyticLineModel.getAnalyticLine();

    if (analyticLine.getAnalyticMoveLineList() == null) {
      analyticLine.setAnalyticMoveLineList(new ArrayList<>());
    } else {
      analyticLineModel.getAnalyticMoveLineList().clear();
    }

    for (AnalyticAccount axisAnalyticAccount : this.getAxisAnalyticAccountList(analyticLine)) {
      AnalyticMoveLine analyticMoveLine =
          this.computeAnalyticMoveLine(analyticLineModel, axisAnalyticAccount);

      analyticLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }

    return true;
  }

  protected List<AnalyticAccount> getAxisAnalyticAccountList(AnalyticLine analyticLine) {
    return Stream.of(
            analyticLine.getAxis1AnalyticAccount(),
            analyticLine.getAxis2AnalyticAccount(),
            analyticLine.getAxis3AnalyticAccount(),
            analyticLine.getAxis4AnalyticAccount(),
            analyticLine.getAxis5AnalyticAccount())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  protected AnalyticMoveLine computeAnalyticMoveLine(
      AnalyticLineModel analyticLineModel, AnalyticAccount analyticAccount) throws AxelorException {
    Company company = analyticLineModel.getCompany();

    AnalyticMoveLine analyticMoveLine =
        analyticMoveLineService.computeAnalytic(company, analyticAccount);
    analyticMoveLineService.setAnalyticCurrency(company, analyticMoveLine);

    analyticMoveLine.setDate(appBaseService.getTodayDate(company));
    analyticMoveLine.setAmount(
        currencyScaleService.getScaledValue(analyticMoveLine, analyticLineModel.getLineAmount()));
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);

    return analyticMoveLine;
  }

  public AnalyticLineModel getAndComputeAnalyticDistribution(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    if (!productAccountManageAnalytic(analyticLineModel)) {
      return analyticLineModel;
    }

    AnalyticLine analyticLine = analyticLineModel.getAnalyticLine();

    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticMoveLineService.getAnalyticDistributionTemplate(
            analyticLineModel.getPartner(),
            analyticLineModel.getProduct(),
            analyticLineModel.getCompany(),
            analyticLineModel.getTradingName(),
            analyticLineModel.getAccount(),
            analyticLineModel.getIsPurchase());

    analyticLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);

    if (analyticLine.getAnalyticMoveLineList() != null) {
      analyticLine.getAnalyticMoveLineList().clear();
    }

    this.computeAnalyticDistribution(analyticLineModel);

    // analyticLineModel.copyToModel();
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
                analyticMoveLine, analyticLineModel.getLineAmount()),
            date);
      }
    }

    // analyticLineModel.copyToModel();

    return analyticLineModel;
  }

  @Override
  public AnalyticLineModel createAnalyticDistributionWithTemplate(
      AnalyticLineModel analyticLineModel) throws AxelorException {
    AnalyticLine analyticLine = analyticLineModel.getAnalyticLine();

    this.clearAnalyticInLine(analyticLine);

    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            analyticLineModel.getAnalyticDistributionTemplate(),
            currencyScaleService.getCompanyScaledValue(
                analyticLineModel.getCompany(), analyticLineModel.getLineAmount()),
            AnalyticMoveLineRepository.STATUS_FORECAST_ORDER,
            appBaseService.getTodayDate(this.getCompany(analyticLineModel)));

    analyticLine.clearAnalyticMoveLineList();
    if (ObjectUtils.notEmpty(analyticMoveLineList)) {
      analyticMoveLineList.forEach(analyticLine::addAnalyticMoveLineListItem);
    }

    return analyticLineModel;
  }

  protected Company getCompany(AnalyticLineModel analyticLineModel) {
    return analyticLineModel.getCompany() != null
        ? analyticLineModel.getCompany()
        : Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
  }

  protected void clearAnalyticInLine(AnalyticLine analyticLine) {
    analyticLine.setAxis1AnalyticAccount(null);
    analyticLine.setAxis2AnalyticAccount(null);
    analyticLine.setAxis3AnalyticAccount(null);
    analyticLine.setAxis4AnalyticAccount(null);
    analyticLine.setAxis5AnalyticAccount(null);
  }

  @Override
  public void setInvoiceLineAnalyticInfo(AnalyticLine analyticLine, InvoiceLine invoiceLine) {
    invoiceLine.setAnalyticDistributionTemplate(analyticLine.getAnalyticDistributionTemplate());

    invoiceLine.setAxis1AnalyticAccount(analyticLine.getAxis1AnalyticAccount());
    invoiceLine.setAxis2AnalyticAccount(analyticLine.getAxis2AnalyticAccount());
    invoiceLine.setAxis3AnalyticAccount(analyticLine.getAxis3AnalyticAccount());
    invoiceLine.setAxis4AnalyticAccount(analyticLine.getAxis4AnalyticAccount());
    invoiceLine.setAxis5AnalyticAccount(analyticLine.getAxis5AnalyticAccount());
  }

  @Override
  public boolean analyticDistributionTemplateRequired(boolean isPurchase, Company company)
      throws AxelorException {
    return analyticToolService.isManageAnalytic(company);
  }

  @Override
  public void checkRequiredAxisByCompany(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    AnalyticLine analyticLine = analyticLineModel.getAnalyticLine();

    if (!CollectionUtils.isEmpty(analyticLine.getAnalyticMoveLineList())) {
      Company company = analyticLineModel.getCompany();
      List<AnalyticAxis> analyticAxisList =
          analyticLine.getAnalyticMoveLineList().stream()
              .map(AnalyticMoveLine::getAnalyticAxis)
              .collect(Collectors.toList());
      analyticAxisService.checkRequiredAxisByCompany(company, analyticAxisList);
    }
  }
}
