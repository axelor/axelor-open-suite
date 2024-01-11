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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
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

  void finishManufOrder(ManufOrder manufOrder) throws AxelorException;

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

  String planManufOrders(List<ManufOrder> manufOrderList) throws AxelorException;

  boolean sendPartialFinishMail(ManufOrder manufOrder);

  boolean sendFinishedMail(ManufOrder manufOrder);
}
