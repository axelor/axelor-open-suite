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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Account;
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
import com.axelor.apps.businessproject.service.config.BusinessProjectConfigService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ProjectAnalyticMoveLineServiceImpl extends AnalyticMoveLineServiceImpl
    implements ProjectAnalyticMoveLineService {

  protected AnalyticMoveLineRepository analyticMoveLineRepository;
  protected BusinessProjectConfigService businessProjectConfigService;
  protected AccountingSituationService accountingSituationService;

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
      AnalyticMoveLineRepository analyticMoveLineRepository1,
      BusinessProjectConfigService businessProjectConfigService,
      AccountingSituationService accountingSituationService1) {
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
    this.analyticMoveLineRepository = analyticMoveLineRepository1;
    this.businessProjectConfigService = businessProjectConfigService;
    this.accountingSituationService = accountingSituationService1;
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
  public AnalyticDistributionTemplate getAnalyticDistributionTemplate(
      Project project,
      Partner partner,
      Product product,
      Company company,
      TradingName tradingName,
      Account account,
      boolean isPurchase)
      throws AxelorException {
    if (company == null || project == null) {
      return null;
    }

    BusinessProjectConfig businessProjectConfig =
        businessProjectConfigService.getBusinessProjectConfig(company);

    if (businessProjectConfig.getUseAssignedToAnalyticDistribution()) {
      User assignedToUser = project.getAssignedTo();
      if (assignedToUser != null && assignedToUser.getEmployee() != null) {
        return assignedToUser.getEmployee().getAnalyticDistributionTemplate();
      }
    }

    AccountingSituation accountingSituation = null;
    if (partner != null) {
      accountingSituation = accountingSituationService.getAccountingSituation(partner, company);
    }

    return accountingSituation != null
        ? accountingSituation.getAnalyticDistributionTemplate()
        : null;
  }
}
