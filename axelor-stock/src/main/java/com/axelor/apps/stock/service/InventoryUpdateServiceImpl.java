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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InventoryUpdateServiceImpl implements InventoryUpdateService {

  protected InventoryService inventoryService;
  protected InventoryProductService inventoryProductService;

  @Inject
  public InventoryUpdateServiceImpl(
      InventoryService inventoryService, InventoryProductService inventoryProductService) {
    this.inventoryService = inventoryService;
    this.inventoryProductService = inventoryProductService;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateInventoryStatus(Inventory inventory, Integer wantedStatus, User user)
      throws AxelorException {
    inventoryProductService.checkDuplicate(inventory);
    if (wantedStatus == InventoryRepository.STATUS_IN_PROGRESS) {
      inventoryService.startInventory(inventory);
    }
    if (wantedStatus == InventoryRepository.STATUS_COMPLETED) {
      inventoryService.completeInventory(inventory);
      if (user != null) {
        inventory.setCompletedBy(user);
      }
    }
    if (wantedStatus == InventoryRepository.STATUS_VALIDATED) {
      inventoryService.validateInventory(inventory);
      if (user != null) {
        inventory.setValidatedBy(user);
      }
    }
  }
}
