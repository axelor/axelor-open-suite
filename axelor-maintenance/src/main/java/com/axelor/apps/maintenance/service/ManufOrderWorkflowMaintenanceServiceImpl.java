/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutgoingStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderTrackingNumberService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderOutsourceService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

public class ManufOrderWorkflowMaintenanceServiceImpl extends ManufOrderWorkflowServiceImpl {

  @Inject
  public ManufOrderWorkflowMaintenanceServiceImpl(
      OperationOrderWorkflowService operationOrderWorkflowService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ManufOrderRepository manufOrderRepo,
      ProductCompanyService productCompanyService,
      ProductionConfigRepository productionConfigRepo,
      AppBaseService appBaseService,
      OperationOrderService operationOrderService,
      AppProductionService appProductionService,
      ProductionConfigService productionConfigService,
      ManufOrderOutgoingStockMoveService manufOrderOutgoingStockMoveService,
      ManufOrderService manufOrderService,
      SequenceService sequenceService,
      ManufOrderOutsourceService manufOrderOutsourceService,
      OperationOrderOutsourceService operationOrderOutsourceService,
      ProductService productService,
      ManufOrderTrackingNumberService manufOrderTrackingNumberService) {
    super(
        operationOrderWorkflowService,
        manufOrderStockMoveService,
        manufOrderRepo,
        productCompanyService,
        productionConfigRepo,
        appBaseService,
        operationOrderService,
        appProductionService,
        productionConfigService,
        manufOrderOutgoingStockMoveService,
        manufOrderService,
        sequenceService,
        manufOrderOutsourceService,
        operationOrderOutsourceService,
        productService,
        manufOrderTrackingNumberService);
  }

  @Override
  public boolean finish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super.finish(manufOrder);
    } else {
      return maintenanceFinishManufOrder(manufOrder);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected boolean maintenanceFinishManufOrder(ManufOrder manufOrder) throws AxelorException {
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

  @Override
  @Transactional(rollbackOn = Exception.class)
  public boolean partialFinish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super.partialFinish(manufOrder);
    } else {
      manufOrderStockMoveService.partialFinish(manufOrder);
      return true;
    }
  }
}
