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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.manuforder.ManufOrderCreatePurchaseOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderOperationOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderPlanServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderPlanStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderProdProductService;
import com.axelor.apps.production.service.manuforder.ManufOrderResidualProductService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderPlanningService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.apps.production.service.productionorder.ProductionOrderService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderPlanServiceMaintenanceImpl extends ManufOrderPlanServiceImpl {

  @Inject
  public ManufOrderPlanServiceMaintenanceImpl(
      ManufOrderRepository manufOrderRepo,
      ManufOrderService manufOrderService,
      SequenceService sequenceService,
      OperationOrderRepository operationOrderRepo,
      OperationOrderWorkflowService operationOrderWorkflowService,
      OperationOrderPlanningService operationOrderPlanningService,
      OperationOrderService operationOrderService,
      ProductionOrderService productionOrderService,
      ProductionConfigService productionConfigService,
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      ManufOrderCreatePurchaseOrderService manufOrderCreatePurchaseOrderService,
      ManufOrderPlanStockMoveService manufOrderPlanStockMoveService,
      ManufOrderResidualProductService manufOrderResidualProductService,
      ManufOrderOperationOrderService manufOrderOperationOrderService,
      ManufOrderProdProductService manufOrderProdProductService,
      PartnerRepository partnerRepository,
      BillOfMaterialService billOfMaterialService) {
    super(
        manufOrderRepo,
        manufOrderService,
        sequenceService,
        operationOrderRepo,
        operationOrderWorkflowService,
        operationOrderPlanningService,
        operationOrderService,
        productionOrderService,
        productionConfigService,
        appBaseService,
        appProductionService,
        manufOrderCreatePurchaseOrderService,
        manufOrderPlanStockMoveService,
        manufOrderResidualProductService,
        manufOrderOperationOrderService,
        manufOrderProdProductService,
        partnerRepository,
        billOfMaterialService);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public ManufOrder plan(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super.plan(manufOrder);
    }

    ManufOrderService manufOrderService = Beans.get(ManufOrderService.class);

    if (Beans.get(SequenceService.class)
        .isEmptyOrDraftSequenceNumber(manufOrder.getManufOrderSeq())) {
      manufOrder.setManufOrderSeq(manufOrderService.getManufOrderSeq(manufOrder));
    }

    if (CollectionUtils.isEmpty(manufOrder.getOperationOrderList())) {
      manufOrderOperationOrderService.preFillOperations(manufOrder);
    }
    if (!manufOrder.getIsConsProOnOperation()
        && CollectionUtils.isEmpty(manufOrder.getToConsumeProdProductList())) {
      manufOrderProdProductService.createToConsumeProdProductList(manufOrder);
    }

    if (CollectionUtils.isEmpty(manufOrder.getToProduceProdProductList())) {
      manufOrderProdProductService.createToProduceProdProductList(manufOrder);
    }

    if (manufOrder.getPlannedStartDateT() == null) {
      manufOrder.setPlannedStartDateT(
          Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
    }

    operationOrderPlanningService.plan(manufOrder.getOperationOrderList());

    manufOrder.setPlannedEndDateT(this.computePlannedEndDateT(manufOrder));

    if (manufOrder.getBillOfMaterial() != null) {
      manufOrder.setUnit(manufOrder.getBillOfMaterial().getUnit());
    }

    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_PLANNED);
    manufOrder.setCancelReason(null);
    manufOrder.setCancelReasonStr(null);

    return manufOrderRepo.save(manufOrder);
  }
}
