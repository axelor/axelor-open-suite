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

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ProjectAnalyticMoveLineServiceImpl extends AnalyticMoveLineServiceImpl
    implements ProjectAnalyticMoveLineService {

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
      CurrencyScaleService currencyScaleService) {
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
}
