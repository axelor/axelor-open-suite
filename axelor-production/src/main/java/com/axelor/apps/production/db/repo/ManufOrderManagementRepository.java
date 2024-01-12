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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;

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
    entity.setWasteStockMove(null);
    entity.setCostPrice(null);
    entity.setBarCode(null);
    if (deep) {
      entity.clearInStockMoveList();
      entity.clearOutStockMoveList();
      entity.clearToConsumeProdProductList();
      entity.clearConsumedStockMoveLineList();
      entity.clearDiffConsumeProdProductList();
      entity.clearToProduceProdProductList();
      entity.clearProducedStockMoveLineList();
      entity.clearWasteProdProductList();
      entity.clearOperationOrderList();
      entity.clearCostSheetList();
    }
    return super.copy(entity, deep);
  }

  @Override
  public ManufOrder save(ManufOrder entity) {
    entity = super.save(entity);

    try {
      if (Strings.isNullOrEmpty(entity.getManufOrderSeq())
          && entity.getStatusSelect() == ManufOrderRepository.STATUS_DRAFT) {
        entity.setManufOrderSeq(Beans.get(SequenceService.class).getDraftSequenceNumber(entity));
      }
      if (entity.getBarCode() == null) {
        Beans.get(ManufOrderService.class).createBarcode(entity);
      }
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    if (entity.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : entity.getOperationOrderList()) {
        if (operationOrder.getBarCode() == null) {
          operationOrderService.createBarcode(operationOrder);
        }
      }
    }
    return super.save(entity);
  }

  @Override
  public void remove(ManufOrder entity) {
    Integer status = entity.getStatusSelect();
    if (status == ManufOrderRepository.STATUS_PLANNED
        || status == ManufOrderRepository.STATUS_STANDBY
        || status == ManufOrderRepository.STATUS_IN_PROGRESS) {
      throw new PersistenceException(I18n.get(ProductionExceptionMessage.ORDER_REMOVE_NOT_OK));
    } else if (status == ManufOrderRepository.STATUS_FINISHED) {
      entity.setArchived(true);
    } else {
      super.remove(entity);
    }
  }
}
