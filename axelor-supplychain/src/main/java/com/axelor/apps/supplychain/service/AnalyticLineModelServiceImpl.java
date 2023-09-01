package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
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

  @Inject
  public AnalyticLineModelServiceImpl(
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      AnalyticToolService analyticToolService) {
    this.appBaseService = appBaseService;
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.analyticToolService = analyticToolService;
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

    analyticMoveLine.setDate(appBaseService.getTodayDate(company));
    analyticMoveLine.setAmount(analyticLineModel.getExTaxTotal());
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);

    return analyticMoveLine;
  }

  public AnalyticLineModel getAndComputeAnalyticDistribution(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    if (!productAccountManageAnalytic(analyticLineModel)
        || isFreeAnalyticDistribution(analyticLineModel)) {
      return analyticLineModel;
    }

    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticMoveLineService.getAnalyticDistributionTemplate(
            analyticLineModel.getPartner(),
            analyticLineModel.getProduct(),
            analyticLineModel.getCompany(),
            analyticLineModel.getTradingName(),
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
        && product.getProductFamily() != null
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
            analyticMoveLine, analyticLineModel.getCompanyExTaxTotal(), date);
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
            analyticLineModel.getExTaxTotal(),
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
}
