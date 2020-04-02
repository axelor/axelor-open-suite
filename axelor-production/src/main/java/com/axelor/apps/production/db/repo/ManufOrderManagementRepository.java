/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.OperationOrderService;
import com.google.inject.Inject;

public class ManufOrderManagementRepository extends ManufOrderRepository {

  @Inject OperationOrderService operationOrderService;

  @Override
  public ManufOrder copy(ManufOrder entity, boolean deep) {
    entity.setStatusSelect(ManufOrderRepository.STATUS_DRAFT);
    entity.setManufOrderSeq(null);
    entity.setPlannedStartDateT(null);
    entity.setPlannedEndDateT(null);
    entity.setRealStartDateT(null);
    entity.setRealEndDateT(null);
    entity.setInStockMoveList(null);
    entity.setOutStockMoveList(null);
    entity.setWasteStockMove(null);
    entity.setToConsumeProdProductList(null);
    entity.setConsumedStockMoveLineList(null);
    entity.setDiffConsumeProdProductList(null);
    entity.setToProduceProdProductList(null);
    entity.setProducedStockMoveLineList(null);
    entity.setWasteProdProductList(null);
    if (entity.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : entity.getOperationOrderList()) {
        operationOrder.setStatusSelect(OperationOrderRepository.STATUS_DRAFT);
        operationOrder.setPlannedStartDateT(null);
        operationOrder.setPlannedEndDateT(null);
        operationOrder.setPlannedDuration(0L);
        operationOrder.setRealStartDateT(null);
        operationOrder.setRealEndDateT(null);
        operationOrder.setRealDuration(0L);
        operationOrder.setOperationOrderDurationList(null);
        operationOrder.setInStockMoveList(null);
        operationOrder.setToConsumeProdProductList(null);
        operationOrder.setConsumedStockMoveLineList(null);
        operationOrder.setDiffConsumeProdProductList(null);
        operationOrder.setBarCode(null);
      }
    }
    return super.copy(entity, deep);
  }

  @Override
  public ManufOrder save(ManufOrder entity) {
    entity = super.save(entity);

    for (OperationOrder operationOrder : entity.getOperationOrderList()) {
      if (operationOrder.getBarCode() == null) {
        operationOrderService.createBarcode(operationOrder);
      }
    }
    return super.save(entity);
  }
}
