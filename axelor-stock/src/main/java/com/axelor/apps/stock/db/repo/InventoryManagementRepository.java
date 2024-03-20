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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.service.InventoryService;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import java.util.List;
import javax.persistence.PersistenceException;

public class InventoryManagementRepository extends InventoryRepository {
  @Override
  public Inventory copy(Inventory entity, boolean deep) {

    Inventory copy = super.copy(entity, deep);
    copy.setStatusSelect(STATUS_DRAFT);
    copy.setInventorySeq(null);
    copy.setValidatedOn(null);
    copy.setValidatedBy(null);

    List<InventoryLine> inventoryLineList = copy.getInventoryLineList();
    if (inventoryLineList != null) {
      inventoryLineList.forEach(this::resetInventoryLineList);
    }
    return copy;
  }

  protected void resetInventoryLineList(InventoryLine inventoryLine) {
    inventoryLine.setCurrentQty(null);
    inventoryLine.setRealQty(null);
    inventoryLine.setGapValue(null);
    inventoryLine.setRealValue(null);
    inventoryLine.setGap(null);
  }

  @Override
  public Inventory save(Inventory entity) {
    Inventory inventory = super.save(entity);
    SequenceService sequenceService = Beans.get(SequenceService.class);

    try {
      if (Strings.isNullOrEmpty(inventory.getInventorySeq())) {
        inventory.setInventorySeq(sequenceService.getDraftSequenceNumber(inventory));
      }
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

    entity.setInventoryTitle(Beans.get(InventoryService.class).computeTitle(entity));

    return inventory;
  }
}
