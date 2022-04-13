/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderWorkflowServiceImpl implements PurchaseOrderWorkflowService {

  protected PurchaseOrderService purchaseOrderService;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected AppPurchaseService appPurchaseService;

  @Inject
  public PurchaseOrderWorkflowServiceImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderRepository purchaseOrderRepo,
      AppPurchaseService appPurchaseService) {
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.appPurchaseService = appPurchaseService;
  }

  @Override
  @Transactional
  public void draftPurchaseOrder(PurchaseOrder purchaseOrder) {

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_DRAFT);
    purchaseOrderRepo.save(purchaseOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    purchaseOrderService.computePurchaseOrder(purchaseOrder);

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_VALIDATED);
    purchaseOrder.setValidationDate(appPurchaseService.getTodayDate(purchaseOrder.getCompany()));
    purchaseOrder.setValidatedByUser(AuthUtils.getUser());

    purchaseOrder.setSupplierPartner(purchaseOrderService.validateSupplier(purchaseOrder));

    purchaseOrderService.updateCostPrice(purchaseOrder);
  }

  @Override
  @Transactional
  public void finishPurchaseOrder(PurchaseOrder purchaseOrder) {
    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_FINISHED);
    purchaseOrderRepo.save(purchaseOrder);
  }

  @Override
  @Transactional
  public void cancelPurchaseOrder(PurchaseOrder purchaseOrder) {
    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_CANCELED);
    purchaseOrderRepo.save(purchaseOrder);
  }
}
