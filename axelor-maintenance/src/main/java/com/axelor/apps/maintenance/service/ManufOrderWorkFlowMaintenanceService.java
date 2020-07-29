/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderWorkFlowMaintenanceService extends ManufOrderWorkflowService {

  @Inject
  public ManufOrderWorkFlowMaintenanceService(
      OperationOrderWorkflowService operationOrderWorkflowService,
      OperationOrderRepository operationOrderRepo,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ManufOrderRepository manufOrderRepo) {
    super(
        operationOrderWorkflowService,
        operationOrderRepo,
        manufOrderStockMoveService,
        manufOrderRepo);
  }

  @Transactional
  @Override
  public ManufOrder plan(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super.plan(manufOrder);
    }

    ManufOrderService manufOrderService = Beans.get(ManufOrderService.class);

    if (Beans.get(SequenceService.class)
        .isEmptyOrDraftSequenceNumber(manufOrder.getManufOrderSeq())) {
      manufOrder.setManufOrderSeq(manufOrderService.getManufOrderSeq(manufOrder));
    }

    if (CollectionUtils.isEmpty(manufOrder.getOperationOrderList())) {
      manufOrderService.preFillOperations(manufOrder);
    }
    if (!manufOrder.getIsConsProOnOperation()
        && CollectionUtils.isEmpty(manufOrder.getToConsumeProdProductList())) {
      manufOrderService.createToConsumeProdProductList(manufOrder);
    }

    if (CollectionUtils.isEmpty(manufOrder.getToProduceProdProductList())) {
      manufOrderService.createToProduceProdProductList(manufOrder);
    }

    if (manufOrder.getPlannedStartDateT() == null) {
      manufOrder.setPlannedStartDateT(
          Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
    }

    for (OperationOrder operationOrder : getSortedOperationOrderList(manufOrder)) {
      operationOrderWorkflowService.plan(operationOrder, null);
    }

    manufOrder.setPlannedEndDateT(this.computePlannedEndDateT(manufOrder));

    if (manufOrder.getBillOfMaterial() != null) {
      manufOrder.setUnit(manufOrder.getBillOfMaterial().getUnit());
    }

    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_PLANNED);
    manufOrder.setCancelReason(null);
    manufOrder.setCancelReasonStr(null);

    return manufOrderRepo.save(manufOrder);
  }

  /**
   * Get a list of operation orders sorted by priority and id from the specified manufacturing
   * order.
   *
   * @param manufOrder
   * @return
   */
  @Override
  protected List<OperationOrder> getSortedOperationOrderList(ManufOrder manufOrder) {
    List<OperationOrder> operationOrderList =
        MoreObjects.firstNonNull(manufOrder.getOperationOrderList(), Collections.emptyList());
    Comparator<OperationOrder> byPriority =
        Comparator.comparing(
            OperationOrder::getPriority, Comparator.nullsFirst(Comparator.naturalOrder()));
    Comparator<OperationOrder> byId =
        Comparator.comparing(
            OperationOrder::getId, Comparator.nullsFirst(Comparator.naturalOrder()));

    return operationOrderList.stream()
        .sorted(byPriority.thenComparing(byId))
        .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public boolean finish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super.finish(manufOrder);
    } else {
      if (manufOrder.getOperationOrderList() != null) {
        for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
          if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_FINISHED) {
            if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_IN_PROGRESS
                && operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_STANDBY) {
              operationOrderWorkflowService.start(operationOrder);
            }
            operationOrderWorkflowService.finish(operationOrder);
          }
        }
      }

      manufOrder.setRealEndDateT(
          Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
      manufOrder.setStatusSelect(ManufOrderRepository.STATUS_FINISHED);
      manufOrder.setEndTimeDifference(
          new BigDecimal(
              ChronoUnit.MINUTES.between(
                  manufOrder.getPlannedEndDateT(), manufOrder.getRealEndDateT())));
      manufOrderRepo.save(manufOrder);
      return true;
    }
  }

  @Override
  public boolean partialFinish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super.partialFinish(manufOrder);
    } else {
      Beans.get(ManufOrderStockMoveService.class).partialFinish(manufOrder);
      return true;
    }
  }
}
