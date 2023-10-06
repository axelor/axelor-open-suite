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
package com.axelor.apps.production.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.operationorder.OperationOrderPlanningService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Singleton
public class OperationOrderController {

  public void computeDuration(ActionRequest request, ActionResponse response) {

    OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
    operationOrder = Beans.get(OperationOrderRepository.class).find(operationOrder.getId());

    Beans.get(OperationOrderPlanningService.class).computeDuration(operationOrder);
    response.setReload(true);
  }

  public void setPlannedDates(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      LocalDateTime plannedStartDateT = operationOrder.getPlannedStartDateT();
      LocalDateTime plannedEndDateT = operationOrder.getPlannedEndDateT();
      operationOrder = Beans.get(OperationOrderRepository.class).find(operationOrder.getId());
      Beans.get(OperationOrderPlanningService.class)
          .setPlannedDates(operationOrder, plannedStartDateT, plannedEndDateT);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setRealDates(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      LocalDateTime realStartDateT = operationOrder.getRealStartDateT();
      LocalDateTime realEndDateT = operationOrder.getRealEndDateT();
      operationOrder = Beans.get(OperationOrderRepository.class).find(operationOrder.getId());
      Beans.get(OperationOrderPlanningService.class)
          .setRealDates(operationOrder, realStartDateT, realEndDateT);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void plan(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      if (operationOrder.getManufOrder() != null
          && operationOrder.getManufOrder().getStatusSelect()
              < ManufOrderRepository.STATUS_PLANNED) {
        return;
      }
      Beans.get(OperationOrderWorkflowService.class)
          .plan(Beans.get(OperationOrderRepository.class).find(operationOrder.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void start(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      operationOrder = Beans.get(OperationOrderRepository.class).find(operationOrder.getId());
      Beans.get(OperationOrderWorkflowService.class).start(operationOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void pause(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      operationOrder = Beans.get(OperationOrderRepository.class).find(operationOrder.getId());
      Beans.get(OperationOrderWorkflowService.class).pause(operationOrder);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void resume(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      operationOrder = Beans.get(OperationOrderRepository.class).find(operationOrder.getId());
      Beans.get(OperationOrderWorkflowService.class).resume(operationOrder);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void finish(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      // this attribute is not in the database, only in the view
      LocalDateTime realStartDateT = operationOrder.getRealStartDateT();
      operationOrder = Beans.get(OperationOrderRepository.class).find(operationOrder.getId());
      operationOrder.setRealStartDateT(realStartDateT);
      Beans.get(OperationOrderWorkflowService.class).finishAndAllOpFinished(operationOrder);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void partialFinish(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      operationOrder = Beans.get(OperationOrderRepository.class).find(operationOrder.getId());

      Beans.get(OperationOrderStockMoveService.class).partialFinish(operationOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      Beans.get(OperationOrderWorkflowService.class)
          .cancel(Beans.get(OperationOrderRepository.class).find(operationOrder.getId()));

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void chargeByMachineHours(ActionRequest request, ActionResponse response) {
    try {
      LocalDateTime fromDateTime =
          LocalDateTime.parse(
              request.getContext().get("fromDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
      LocalDateTime toDateTime =
          LocalDateTime.parse(
              request.getContext().get("toDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);

      List<Map<String, Object>> dataList =
          Beans.get(OperationOrderService.class).chargeByMachineHours(fromDateTime, toDateTime);

      response.setData(dataList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void chargeByMachineDays(ActionRequest request, ActionResponse response) {

    try {
      LocalDateTime fromDateTime =
          LocalDateTime.parse(
              request.getContext().get("fromDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);
      LocalDateTime toDateTime =
          LocalDateTime.parse(
              request.getContext().get("toDateTime").toString(), DateTimeFormatter.ISO_DATE_TIME);

      List<Map<String, Object>> dataList =
          Beans.get(OperationOrderService.class).chargeByMachineDays(fromDateTime, toDateTime);
      response.setData(dataList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from operation order form, on consumed stock move line change. Call {@link
   * OperationOrderService#checkConsumedStockMoveLineList(OperationOrder, OperationOrder)}
   *
   * @param request
   * @param response
   */
  public void checkConsumedStockMoveLineList(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      OperationOrder oldOperationOrder =
          Beans.get(OperationOrderRepository.class).find(operationOrder.getId());
      Beans.get(OperationOrderService.class)
          .checkConsumedStockMoveLineList(operationOrder, oldOperationOrder);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setReload(true);
    }
  }
  /**
   * Called from operation order form, on consumed stock move line change.
   *
   * @param request
   * @param response
   */
  public void updateConsumedStockMoveFromOperationOrder(
      ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      operationOrder = Beans.get(OperationOrderRepository.class).find(operationOrder.getId());
      Beans.get(OperationOrderService.class)
          .updateConsumedStockMoveFromOperationOrder(operationOrder);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
