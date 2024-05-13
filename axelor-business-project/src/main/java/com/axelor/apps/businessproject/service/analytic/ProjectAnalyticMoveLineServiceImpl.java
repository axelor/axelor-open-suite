/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.db.BusinessProjectConfig;
import com.axelor.apps.businessproject.model.AnalyticLineProjectModel;
import com.axelor.apps.businessproject.service.config.BusinessProjectConfigService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ProjectAnalyticMoveLineServiceImpl extends AnalyticMoveLineServiceImpl
    implements ProjectAnalyticMoveLineService {

  protected BusinessProjectConfigService businessProjectConfigService;
  protected AnalyticLineModelFromEmployeeService analyticLineModelFromEmployeeService;

  @Inject
  public ProjectAnalyticMoveLineServiceImpl(
      AnalyticMoveLineRepository analyticMoveLineRepository,
      AppAccountService appAccountService,
      AccountManagementServiceAccountImpl accountManagementServiceAccountImpl,
      AccountConfigService accountConfigService,
      AccountConfigRepository accountConfigRepository,
      AccountRepository accountRepository,
      AppBaseService appBaseService,
      AccountingSituationService accountingSituationService,
      CurrencyScaleService currencyScaleService,
      BusinessProjectConfigService businessProjectConfigService,
      AnalyticLineModelFromEmployeeService analyticLineModelFromEmployeeService) {
    super(
        analyticMoveLineRepository,
        appAccountService,
        accountManagementServiceAccountImpl,
        accountConfigService,
        accountConfigRepository,
        accountRepository,
        appBaseService,
        accountingSituationService,
        currencyScaleService);
    this.businessProjectConfigService = businessProjectConfigService;
    this.analyticLineModelFromEmployeeService = analyticLineModelFromEmployeeService;
  }

  @Override
  @Transactional
  public PurchaseOrder updateLines(PurchaseOrder purchaseOrder) {
    for (PurchaseOrderLine orderLine : purchaseOrder.getPurchaseOrderLineList()) {
      orderLine.setProject(purchaseOrder.getProject());
      for (AnalyticMoveLine analyticMoveLine : orderLine.getAnalyticMoveLineList()) {
        analyticMoveLine.setProject(purchaseOrder.getProject());
        analyticMoveLineRepository.save(analyticMoveLine);
      }
    }
    return purchaseOrder;
  }

  @Override
  @Transactional
  public SaleOrder updateLines(SaleOrder saleOrder) {
    for (SaleOrderLine orderLine : saleOrder.getSaleOrderLineList()) {
      orderLine.setProject(saleOrder.getProject());
      List<AnalyticMoveLine> analyticMoveLines = orderLine.getAnalyticMoveLineList();
      if (ObjectUtils.notEmpty(analyticMoveLines)) {
        for (AnalyticMoveLine analyticMoveLine : analyticMoveLines) {
          analyticMoveLine.setProject(saleOrder.getProject());
          analyticMoveLineRepository.save(analyticMoveLine);
        }
      }
    }
    return saleOrder;
  }

  @Override
  public void fillAnalyticLineProjectModel(AnalyticLineProjectModel analyticLineProjectModel)
      throws AxelorException {
    Project project = analyticLineProjectModel.getProject();
    Partner partner = analyticLineProjectModel.getPartner();
    Product product = analyticLineProjectModel.getProduct();
    Company company = analyticLineProjectModel.getCompany();
    TradingName tradingName = analyticLineProjectModel.getTradingName();
    Account account = analyticLineProjectModel.getAccount();
    boolean isPurchase = analyticLineProjectModel.getIsPurchase();

    if (company == null || project == null) {
      return;
    }
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    AnalyticDistributionTemplate analyticDistributionTemplate = null;

    if (accountConfig.getAnalyticDistributionTypeSelect()
        == AccountConfigRepository.DISTRIBUTION_TYPE_PARTNER) {
      BusinessProjectConfig businessProjectConfig =
          businessProjectConfigService.getBusinessProjectConfig(company);

      if (businessProjectConfig.getUseAssignedToAnalyticDistribution()) {
        User assignedToUser = project.getAssignedTo();
        if (assignedToUser == null) {
          clearAnalyticLineModel(analyticLineProjectModel);
          return;
        }
        Employee employee = assignedToUser.getEmployee();
        if (employee != null) {
          analyticLineModelFromEmployeeService.copyAnalyticsDataFromEmployee(
              employee, analyticLineProjectModel);
          return;
        }
      }

      AccountingSituation accountingSituation = null;
      if (partner != null) {
        accountingSituation = accountingSituationService.getAccountingSituation(partner, company);
      }
      if (accountingSituation != null) {
        analyticDistributionTemplate = accountingSituation.getAnalyticDistributionTemplate();
      }
    } else {
      analyticDistributionTemplate =
          super.getAnalyticDistributionTemplate(
              partner, product, company, tradingName, account, isPurchase);
    }
    analyticLineProjectModel.setAnalyticDistributionTemplate(analyticDistributionTemplate);
  }

  protected void clearAnalyticLineModel(AnalyticLineProjectModel analyticLineProjectModel) {
    analyticLineProjectModel.setAnalyticDistributionTemplate(null);
    analyticLineProjectModel.setAxis1AnalyticAccount(null);
    analyticLineProjectModel.setAxis2AnalyticAccount(null);
    analyticLineProjectModel.setAxis3AnalyticAccount(null);
    analyticLineProjectModel.setAxis4AnalyticAccount(null);
    analyticLineProjectModel.setAxis5AnalyticAccount(null);
    analyticLineProjectModel.clearAnalyticMoveLineList();
  }
}
