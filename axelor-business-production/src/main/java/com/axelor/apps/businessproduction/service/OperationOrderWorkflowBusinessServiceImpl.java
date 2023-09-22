package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.repo.MachineToolRepository;
import com.axelor.apps.production.db.repo.OperationOrderDurationRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.production.service.operationorder.OperationOrderPlanningService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowServiceImpl;
import com.google.inject.Inject;

public class OperationOrderWorkflowBusinessServiceImpl extends OperationOrderWorkflowServiceImpl {

  protected TimesheetLineBusinessProductionService tslBusinessProductionService;

  @Inject
  public OperationOrderWorkflowBusinessServiceImpl(
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepo,
      OperationOrderDurationRepository operationOrderDurationRepo,
      AppProductionService appProductionService,
      MachineToolRepository machineToolRepo,
      ManufOrderWorkflowService manufOrderWorkflowService,
      OperationOrderService operationOrderService,
      OperationOrderPlanningService operationOrderPlanningService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      TimesheetLineBusinessProductionService tslBusinessProductionService) {
    super(
        operationOrderStockMoveService,
        operationOrderRepo,
        operationOrderDurationRepo,
        appProductionService,
        machineToolRepo,
        manufOrderWorkflowService,
        operationOrderService,
        operationOrderPlanningService,
        manufOrderStockMoveService);

    this.tslBusinessProductionService = tslBusinessProductionService;
  }

  @Override
  public void stopOperationOrderDuration(OperationOrderDuration duration) throws AxelorException {

    super.stopOperationOrderDuration(duration);
    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getManageBusinessProduction()
        && appProductionService.getAppProduction().getAutoGenerateTimesheetLine()) {

      tslBusinessProductionService
          .createTimesheetLine(duration)
          .ifPresent(
              tsl -> {
                if (duration.getOperationOrder() != null) {
                  duration.getOperationOrder().addTimesheetLineListItem(tsl);
                }
              });
    }
  }
}
