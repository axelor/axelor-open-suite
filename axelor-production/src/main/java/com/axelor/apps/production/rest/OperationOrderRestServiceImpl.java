/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.rest.dto.OperationOrderResponse;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.apps.production.translation.ITranslation;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import javax.ws.rs.core.Response;

public class OperationOrderRestServiceImpl implements OperationOrderRestService {

  protected OperationOrderWorkflowService operationOrderWorkflowService;

  @Inject
  public OperationOrderRestServiceImpl(
      OperationOrderWorkflowService operationOrderWorkflowService) {
    this.operationOrderWorkflowService = operationOrderWorkflowService;
  }

  public Response updateStatusOfOperationOrder(OperationOrder operationOrder, Integer targetStatus)
      throws AxelorException {
    if ((operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_PLANNED
            || operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS)
        && targetStatus == OperationOrderRepository.STATUS_IN_PROGRESS) {
      operationOrderWorkflowService.start(operationOrder);
    } else if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS
        && targetStatus == OperationOrderRepository.STATUS_STANDBY) {
      operationOrderWorkflowService.pause(operationOrder, AuthUtils.getUser());
      // Operation order not paused
      if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_STANDBY) {
        return ResponseConstructor.build(
            Response.Status.OK,
            I18n.get(ITranslation.OPERATION_ORDER_DURATION_PAUSED_200),
            new OperationOrderResponse((operationOrder)));
      }
    } else if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_STANDBY
        && targetStatus == OperationOrderRepository.STATUS_IN_PROGRESS) {
      operationOrderWorkflowService.resume(operationOrder);
    } else if ((operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS
            || operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_STANDBY)
        && targetStatus == OperationOrderRepository.STATUS_FINISHED) {
      operationOrderWorkflowService.finish(operationOrder, AuthUtils.getUser());

      if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_FINISHED) {
        return ResponseConstructor.build(
            Response.Status.FORBIDDEN,
            I18n.get(ITranslation.OPERATION_ORDER_DURATION_PAUSED_403),
            new OperationOrderResponse((operationOrder)));
      }
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ITranslation.OPERATION_ORDER_WORKFLOW_NOT_SUPPORTED));
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        "Operation order status successfully updated.",
        new OperationOrderResponse((operationOrder)));
  }
}
