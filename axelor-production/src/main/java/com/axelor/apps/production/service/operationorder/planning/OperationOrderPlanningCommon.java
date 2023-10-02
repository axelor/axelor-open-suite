package com.axelor.apps.production.service.operationorder.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.google.inject.Inject;

public abstract class OperationOrderPlanningCommon {

  protected OperationOrderService operationOrderService;
  protected OperationOrderStockMoveService operationOrderStockMoveService;
  protected OperationOrderRepository operationOrderRepository;

  @Inject
  protected OperationOrderPlanningCommon(
      OperationOrderService operationOrderService,
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepository) {
    this.operationOrderService = operationOrderService;
    this.operationOrderStockMoveService = operationOrderStockMoveService;
    this.operationOrderRepository = operationOrderRepository;
  }

  protected abstract void planWithStrategy(OperationOrder operationOrder) throws AxelorException;

  public OperationOrder plan(OperationOrder operationOrder) throws AxelorException {
    planWithStrategy(operationOrder);

    ManufOrder manufOrder = operationOrder.getManufOrder();
    if (manufOrder != null && Boolean.TRUE.equals(manufOrder.getIsConsProOnOperation())) {
      operationOrderStockMoveService.createToConsumeStockMove(operationOrder);
    }
    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_PLANNED);

    return operationOrderRepository.save(operationOrder);
  }
}
