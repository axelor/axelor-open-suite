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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.supplychain.service.PurchaseOrderFromSaleOrderLinesService;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PurchaseOrderServiceProjectImpl implements PurchaseOrderProjectService {

  protected PurchaseOrderRepository purchaseOrderRepository;
  protected PurchaseOrderLineProjectService purchaseOrderLineProjectService;
  protected PurchaseOrderFromSaleOrderLinesService purchaseOrderFromSaleOrderLinesService;

  protected PurchaseOrderService purchaseOrderService;

  @Inject
  public PurchaseOrderServiceProjectImpl(
      PurchaseOrderRepository purchaseOrderRepository,
      PurchaseOrderLineProjectService purchaseOrderLineProjectService,
      PurchaseOrderFromSaleOrderLinesService purchaseOrderFromSaleOrderLinesService,
      PurchaseOrderService purchaseOrderService) {
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.purchaseOrderLineProjectService = purchaseOrderLineProjectService;
    this.purchaseOrderFromSaleOrderLinesService = purchaseOrderFromSaleOrderLinesService;
    this.purchaseOrderService = purchaseOrderService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setProjectAndProjectTask(
      Long purchaseOrderId, Project project, ProjectTask projectTask) {
    PurchaseOrder purchaseOrder = purchaseOrderRepository.find(purchaseOrderId);
    purchaseOrder.setProject(project);
    purchaseOrder.setProjectTask(projectTask);
    List<Long> purchaseOrderLineIdList =
        purchaseOrder.getPurchaseOrderLineList().stream()
            .map(PurchaseOrderLine::getId)
            .collect(Collectors.toList());
    purchaseOrderLineProjectService.setProject(purchaseOrderLineIdList, project);
    purchaseOrderLineProjectService.setProjectTask(purchaseOrderLineIdList, projectTask);
    purchaseOrderRepository.save(purchaseOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  public Map<String, Object> generateEmptyPurchaseOrderFromProjectTask(
      ProjectTask projectTask, Partner supplierPartner) throws AxelorException {
    if (supplierPartner == null) {
      return purchaseOrderFromSaleOrderLinesService.selectSupplierPartner(null, null, null);
    }
    PurchaseOrder purchaseOrder =
        purchaseOrderRepository.save(
            purchaseOrderService.createPurchaseOrder(
                AuthUtils.getUser(),
                projectTask.getProject().getCompany(),
                null,
                supplierPartner.getCurrency(),
                null,
                null,
                null,
                null,
                null,
                supplierPartner,
                null));
    return purchaseOrderFromSaleOrderLinesService.showPurchaseOrderForm(purchaseOrder);
  }
}
