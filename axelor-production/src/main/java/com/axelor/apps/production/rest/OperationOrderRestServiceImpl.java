/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
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
