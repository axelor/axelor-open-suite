package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.exception.AxelorException;
import java.time.LocalDateTime;
import java.util.List;

public interface ManufOrderWorkflowService {

  ManufOrder plan(ManufOrder manufOrder) throws AxelorException;

  List<ManufOrder> plan(List<ManufOrder> manufOrderList) throws AxelorException;

  List<ManufOrder> plan(List<ManufOrder> manufOrderList, boolean quickSolve) throws AxelorException;

  void start(ManufOrder manufOrder) throws AxelorException;

  void pause(ManufOrder manufOrder);

  void resume(ManufOrder manufOrder);

  boolean finish(ManufOrder manufOrder) throws AxelorException;

  boolean partialFinish(ManufOrder manufOrder) throws AxelorException;

  void cancel(ManufOrder manufOrder, CancelReason cancelReason, String cancelReasonStr)
      throws AxelorException;

  LocalDateTime computePlannedStartDateT(ManufOrder manufOrder);

  LocalDateTime computePlannedEndDateT(ManufOrder manufOrder);

  void allOpFinished(ManufOrder manufOrder) throws AxelorException;

  OperationOrder getFirstOperationOrder(ManufOrder manufOrder);

  OperationOrder getLastOperationOrder(ManufOrder manufOrder);

  void updatePlannedDates(ManufOrder manufOrder, LocalDateTime plannedStartDateT)
      throws AxelorException;

  void createPurchaseOrder(ManufOrder manufOrder) throws AxelorException;
}
