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
package com.axelor.apps.production.web;

import static com.axelor.apps.production.exceptions.ProductionExceptionMessage.LAST_MANUFACTURING_OPERATION_PLANNED_END_DATE_WILL_OVERFLOW_BEYOND_THE_MANUF_ORDER_PLANNED_END_DATE;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationPlanningService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationStockMoveService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationWorkflowService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ManufacturingOperationController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void computeDuration(ActionRequest request, ActionResponse response) {

    ManufacturingOperation manufacturingOperation =
        request.getContext().asType(ManufacturingOperation.class);
    manufacturingOperation =
        Beans.get(ManufacturingOperationRepository.class).find(manufacturingOperation.getId());

    Beans.get(ManufacturingOperationPlanningService.class).computeDuration(manufacturingOperation);
    response.setReload(true);
  }

  public void alertPlannedEndDateOverflow(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      if (Beans.get(ManufacturingOperationPlanningService.class)
          .willPlannedEndDateOverflow(manufacturingOperation)) {
        response.setAlert(
            I18n.get(
                LAST_MANUFACTURING_OPERATION_PLANNED_END_DATE_WILL_OVERFLOW_BEYOND_THE_MANUF_ORDER_PLANNED_END_DATE));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setPlannedDates(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      LocalDateTime plannedStartDateT = manufacturingOperation.getPlannedStartDateT();
      LocalDateTime plannedEndDateT = manufacturingOperation.getPlannedEndDateT();
      manufacturingOperation =
          Beans.get(ManufacturingOperationRepository.class).find(manufacturingOperation.getId());
      Beans.get(ManufacturingOperationPlanningService.class)
          .setPlannedDates(manufacturingOperation, plannedStartDateT, plannedEndDateT);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setRealDates(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      LocalDateTime realStartDateT = manufacturingOperation.getRealStartDateT();
      LocalDateTime realEndDateT = manufacturingOperation.getRealEndDateT();
      manufacturingOperation =
          Beans.get(ManufacturingOperationRepository.class).find(manufacturingOperation.getId());
      Beans.get(ManufacturingOperationPlanningService.class)
          .setRealDates(manufacturingOperation, realStartDateT, realEndDateT);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void plan(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      if (manufacturingOperation.getManufOrder() != null
          && manufacturingOperation.getManufOrder().getStatusSelect()
              < ManufOrderRepository.STATUS_PLANNED) {
        return;
      }
      Beans.get(ManufacturingOperationWorkflowService.class)
          .plan(
              Beans.get(ManufacturingOperationRepository.class)
                  .find(manufacturingOperation.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void start(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      manufacturingOperation =
          Beans.get(ManufacturingOperationRepository.class).find(manufacturingOperation.getId());
      Beans.get(ManufacturingOperationWorkflowService.class).start(manufacturingOperation);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void pause(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      manufacturingOperation =
          Beans.get(ManufacturingOperationRepository.class).find(manufacturingOperation.getId());
      Beans.get(ManufacturingOperationWorkflowService.class).pause(manufacturingOperation);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void resume(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      manufacturingOperation =
          Beans.get(ManufacturingOperationRepository.class).find(manufacturingOperation.getId());
      Beans.get(ManufacturingOperationWorkflowService.class).resume(manufacturingOperation);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void finish(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      // this attribute is not in the database, only in the view
      LocalDateTime realStartDateT = manufacturingOperation.getRealStartDateT();
      manufacturingOperation =
          Beans.get(ManufacturingOperationRepository.class).find(manufacturingOperation.getId());
      manufacturingOperation.setRealStartDateT(realStartDateT);
      Beans.get(ManufacturingOperationWorkflowService.class)
          .finishAndAllOpFinished(manufacturingOperation);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void partialFinish(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      manufacturingOperation =
          Beans.get(ManufacturingOperationRepository.class).find(manufacturingOperation.getId());

      Beans.get(ManufacturingOperationStockMoveService.class).partialFinish(manufacturingOperation);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      Beans.get(ManufacturingOperationWorkflowService.class)
          .cancel(
              Beans.get(ManufacturingOperationRepository.class)
                  .find(manufacturingOperation.getId()));

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Called from operation order form, on consumed stock move line change. Call {@link
   * ManufacturingOperationService#checkConsumedStockMoveLineList(ManufacturingOperation,
   * ManufacturingOperation)}
   *
   * @param request
   * @param response
   */
  public void checkConsumedStockMoveLineList(ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      ManufacturingOperation oldManufacturingOperation =
          Beans.get(ManufacturingOperationRepository.class).find(manufacturingOperation.getId());
      Beans.get(ManufacturingOperationService.class)
          .checkConsumedStockMoveLineList(manufacturingOperation, oldManufacturingOperation);
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
  public void updateConsumedStockMoveFromManufacturingOperation(
      ActionRequest request, ActionResponse response) {
    try {
      ManufacturingOperation manufacturingOperation =
          request.getContext().asType(ManufacturingOperation.class);
      manufacturingOperation =
          Beans.get(ManufacturingOperationRepository.class).find(manufacturingOperation.getId());
      Beans.get(ManufacturingOperationService.class)
          .updateConsumedStockMoveFromManufacturingOperation(manufacturingOperation);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
