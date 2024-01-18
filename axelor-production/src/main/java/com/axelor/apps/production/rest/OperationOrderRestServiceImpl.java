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
package com.axelor.apps.production.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.rest.dto.OperationOrderResponse;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.apps.production.translation.ITranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.utils.api.ResponseConstructor;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import javax.ws.rs.core.Response;

public class OperationOrderRestServiceImpl implements OperationOrderRestService {

  protected OperationOrderWorkflowService operationOrderWorkflowService;
  protected ManufOrderWorkflowService manufOrderWorkflowService;

  @Inject
  public OperationOrderRestServiceImpl(
      OperationOrderWorkflowService operationOrderWorkflowService,
      ManufOrderWorkflowService manufOrderWorkflowService) {
    this.operationOrderWorkflowService = operationOrderWorkflowService;
    this.manufOrderWorkflowService = manufOrderWorkflowService;
  }

  public Response updateStatusOfOperationOrder(OperationOrder operationOrder, Integer targetStatus)
      throws AxelorException {
    ManufOrder manufOrder = operationOrder.getManufOrder();
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
      finishProcess(operationOrder, manufOrder);
      manufOrderWorkflowService.sendFinishedMail(manufOrder);

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

  @Transactional(rollbackOn = {Exception.class})
  protected void finishProcess(OperationOrder operationOrder, ManufOrder manufOrder)
      throws AxelorException {
    operationOrderWorkflowService.finish(operationOrder, AuthUtils.getUser());
    manufOrderWorkflowService.allOpFinished(manufOrder);
  }
}
