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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

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
  @Transactional(rollbackOn = {Exception.class})
  public void draftPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {

    if (purchaseOrder.getStatusSelect() == null
        || purchaseOrder.getStatusSelect() != PurchaseOrderRepository.STATUS_CANCELED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_DRAFT_WRONG_STATUS));
    }

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_DRAFT);
    purchaseOrderRepo.save(purchaseOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {

    if (purchaseOrder.getStatusSelect() == null
        || purchaseOrder.getStatusSelect() != PurchaseOrderRepository.STATUS_REQUESTED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_VALIDATE_WRONG_STATUS));
    }

    purchaseOrderService.computePurchaseOrder(purchaseOrder);

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_VALIDATED);
    purchaseOrder.setValidationDateTime(
        appPurchaseService.getTodayDateTime(purchaseOrder.getCompany()).toLocalDateTime());
    purchaseOrder.setValidatedByUser(AuthUtils.getUser());

    purchaseOrder.setSupplierPartner(purchaseOrderService.validateSupplier(purchaseOrder));

    purchaseOrderService.updateCostPrice(purchaseOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void finishPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {

    if (purchaseOrder.getStatusSelect() == null
        || purchaseOrder.getStatusSelect() != PurchaseOrderRepository.STATUS_VALIDATED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_FINISH_WRONG_STATUS));
    }

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_FINISHED);
    purchaseOrderRepo.save(purchaseOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(PurchaseOrderRepository.STATUS_DRAFT);
    authorizedStatus.add(PurchaseOrderRepository.STATUS_REQUESTED);
    authorizedStatus.add(PurchaseOrderRepository.STATUS_VALIDATED);
    if (purchaseOrder.getStatusSelect() == null
        || !authorizedStatus.contains(purchaseOrder.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_CANCEL_WRONG_STATUS));
    }

    purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_CANCELED);
    purchaseOrderRepo.save(purchaseOrder);
  }
}
