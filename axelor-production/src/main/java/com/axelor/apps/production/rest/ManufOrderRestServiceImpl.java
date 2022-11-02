package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;

public class ManufOrderRestServiceImpl implements ManufOrderRestService {

  protected ManufOrderWorkflowService manufOrderWorkflowService;

  @Inject
  public ManufOrderRestServiceImpl(ManufOrderWorkflowService manufOrderWorkflowService) {
    this.manufOrderWorkflowService = manufOrderWorkflowService;
  }

  public void updateStatusOfManufOrder(ManufOrder manufOrder, int targetStatus)
      throws AxelorException {
    if (manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_DRAFT
        && targetStatus == ManufOrderRepository.STATUS_PLANNED) {
      manufOrderWorkflowService.plan(manufOrder);
    } else if (manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_PLANNED
        && targetStatus == ManufOrderRepository.STATUS_IN_PROGRESS) {
      manufOrderWorkflowService.start(manufOrder);
    } else if (manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_IN_PROGRESS
        && targetStatus == ManufOrderRepository.STATUS_STANDBY) {
      manufOrderWorkflowService.pause(manufOrder);
    } else if (manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_STANDBY
        && targetStatus == ManufOrderRepository.STATUS_IN_PROGRESS) {
      manufOrderWorkflowService.resume(manufOrder);
    } else if (manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_IN_PROGRESS
        && targetStatus == ManufOrderRepository.STATUS_FINISHED) {
      manufOrderWorkflowService.finish(manufOrder);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          "This workflow is not supported for manufacturing order status.");
    }
  }
}
