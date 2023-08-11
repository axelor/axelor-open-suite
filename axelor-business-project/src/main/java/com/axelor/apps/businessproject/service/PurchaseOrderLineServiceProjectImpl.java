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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.util.List;

public class PurchaseOrderLineServiceProjectImpl extends PurchaseOrderLineServiceSupplychainImpl
    implements PurchaseOrderLineProjectService {

  protected PurchaseOrderLineRepository purchaseOrderLineRepo;

  public PurchaseOrderLineServiceProjectImpl(
      AnalyticMoveLineService analyticMoveLineService,
      UnitConversionService unitConversionService,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      AnalyticLineModelService analyticLineModelService,
      PurchaseOrderLineRepository purchaseOrderLineRepo) {
    super(
        analyticMoveLineService,
        unitConversionService,
        appAccountService,
        accountConfigService,
        analyticLineModelService);
    this.purchaseOrderLineRepo = purchaseOrderLineRepo;
  }

  @Override
  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder, SaleOrderLine saleOrderLine) throws AxelorException {

    PurchaseOrderLine line = super.createPurchaseOrderLine(purchaseOrder, saleOrderLine);

    if (line != null
        && saleOrderLine != null
        && Beans.get(AppBusinessProjectService.class).isApp("business-project")) {
      line.setProject(saleOrderLine.getProject());
    }

    return line;
  }

  @Transactional
  @Override
  public void setProject(List<Long> purchaseOrderLineIds, Project project) {

    if (purchaseOrderLineIds != null) {

      List<PurchaseOrderLine> purchaseOrderLineList =
          purchaseOrderLineRepo
              .all()
              .filter("self.id in :purchaseOrderLineIds")
              .bind("purchaseOrderLineIds", purchaseOrderLineIds)
              .fetch();

      for (PurchaseOrderLine line : purchaseOrderLineList) {
        line.setProject(project);
        purchaseOrderLineRepo.save(line);
      }
    }
  }

  @Transactional
  @Override
  public void setProjectTask(List<Long> purchaseOrderLineIds, ProjectTask projectTask) {

    if (purchaseOrderLineIds != null) {

      List<PurchaseOrderLine> purchaseOrderLineList =
          purchaseOrderLineRepo
              .all()
              .filter("self.id in :purchaseOrderLineIds")
              .bind("purchaseOrderLineIds", purchaseOrderLineIds)
              .fetch();

      for (PurchaseOrderLine line : purchaseOrderLineList) {
        line.setProjectTask(projectTask);
        purchaseOrderLineRepo.save(line);
      }
    }
  }

  @Override
  public PurchaseOrderLine updateAnalyticDistributionWithProject(
      PurchaseOrderLine purchaseOrderLine) {
    for (AnalyticMoveLine analyticMoveLine : purchaseOrderLine.getAnalyticMoveLineList()) {
      analyticMoveLine.setProject(purchaseOrderLine.getProject());
    }
    return purchaseOrderLine;
  }
}
