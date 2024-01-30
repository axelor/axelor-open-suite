package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.manuforder.ManufOrderCreatePurchaseOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderPlanServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
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
      ManufOrderStockMoveService manufOrderStockMoveService,
      ProductionOrderService productionOrderService,
      ProductionConfigService productionConfigService,
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      ManufOrderCreatePurchaseOrderService manufOrderCreatePurchaseOrderService) {
    super(
        manufOrderRepo,
        manufOrderService,
        sequenceService,
        operationOrderRepo,
        operationOrderWorkflowService,
        operationOrderPlanningService,
        operationOrderService,
        manufOrderStockMoveService,
        productionOrderService,
        productionConfigService,
        appBaseService,
        appProductionService,
        manufOrderCreatePurchaseOrderService);
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
      manufOrderService.preFillOperations(manufOrder);
    }
    if (!manufOrder.getIsConsProOnOperation()
        && CollectionUtils.isEmpty(manufOrder.getToConsumeProdProductList())) {
      manufOrderService.createToConsumeProdProductList(manufOrder);
    }

    if (CollectionUtils.isEmpty(manufOrder.getToProduceProdProductList())) {
      manufOrderService.createToProduceProdProductList(manufOrder);
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
