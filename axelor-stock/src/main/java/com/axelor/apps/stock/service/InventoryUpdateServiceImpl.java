package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
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
