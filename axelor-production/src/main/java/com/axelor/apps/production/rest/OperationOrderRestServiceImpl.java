package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;

public class OperationOrderRestServiceImpl implements OperationOrderRestService {

  protected OperationOrderWorkflowService operationOrderWorkflowService;

  @Inject
  public OperationOrderRestServiceImpl(
      OperationOrderWorkflowService operationOrderWorkflowService) {
    this.operationOrderWorkflowService = operationOrderWorkflowService;
  }

  public void updateStatusOfOperationOrder(OperationOrder operationOrder, Integer targetStatus)
      throws AxelorException {
    if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_PLANNED
        && targetStatus == OperationOrderRepository.STATUS_IN_PROGRESS) {
      operationOrderWorkflowService.start(operationOrder);
    } else if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS
        && targetStatus == OperationOrderRepository.STATUS_STANDBY) {
      operationOrderWorkflowService.pause(operationOrder);
    } else if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_STANDBY
        && targetStatus == OperationOrderRepository.STATUS_IN_PROGRESS) {
      operationOrderWorkflowService.resume(operationOrder);
    } else if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS
        && targetStatus == OperationOrderRepository.STATUS_FINISHED) {
      operationOrderWorkflowService.finish(operationOrder);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          "This workflow is not supported for operation order status.");
    }
  }
}
