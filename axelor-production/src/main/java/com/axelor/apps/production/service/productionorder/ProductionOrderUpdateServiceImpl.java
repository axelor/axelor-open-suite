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
package com.axelor.apps.production.service.productionorder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.google.inject.Inject;

public class ProductionOrderUpdateServiceImpl implements ProductionOrderUpdateService {

  protected ProductionOrderRepository productionOrderRepo;

  @Inject
  public ProductionOrderUpdateServiceImpl(ProductionOrderRepository productionOrderRepo) {
    this.productionOrderRepo = productionOrderRepo;
  }

  @Override
  public ProductionOrder addManufOrder(ProductionOrder productionOrder, ManufOrder manufOrder) {
    if (manufOrder != null) {
      productionOrder.addManufOrderSetItem(manufOrder);
      manufOrder.addProductionOrderSetItem(productionOrder);
    }

    productionOrder = updateProductionOrderStatus(productionOrder);
    return productionOrderRepo.save(productionOrder);
  }

  @Override
  public ProductionOrder updateProductionOrderStatus(ProductionOrder productionOrder) {

    if (productionOrder.getStatusSelect() == null) {
      return productionOrder;
    }

    int statusSelect = productionOrder.getStatusSelect();

    if (productionOrder.getManufOrderSet().stream()
        .allMatch(
            manufOrder -> manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_DRAFT)) {
      statusSelect = ProductionOrderRepository.STATUS_DRAFT;
      productionOrder.setStatusSelect(statusSelect);
      return productionOrderRepo.save(productionOrder);
    }

    boolean oneStarted = false;
    boolean onePlanned = false;
    boolean allCancel = true;
    boolean allCompleted = true;

    for (ManufOrder manufOrder : productionOrder.getManufOrderSet()) {

      switch (manufOrder.getStatusSelect()) {
        case (ManufOrderRepository.STATUS_PLANNED):
          onePlanned = true;
          allCancel = false;
          allCompleted = false;
          break;
        case (ManufOrderRepository.STATUS_IN_PROGRESS):
        case (ManufOrderRepository.STATUS_STANDBY):
          oneStarted = true;
          allCancel = false;
          allCompleted = false;
          break;
        case (ManufOrderRepository.STATUS_FINISHED):
          allCancel = false;
          break;
        case (ManufOrderRepository.STATUS_CANCELED):
          break;
        default:
          allCompleted = false;
          break;
      }
    }

    if (allCancel) {
      statusSelect = ProductionOrderRepository.STATUS_CANCELED;
    } else if (allCompleted) {
      statusSelect = ProductionOrderRepository.STATUS_COMPLETED;
    } else if (oneStarted) {
      statusSelect = ProductionOrderRepository.STATUS_STARTED;
    } else if (onePlanned
        && (productionOrder.getStatusSelect() == ProductionOrderRepository.STATUS_DRAFT
            || productionOrder.getStatusSelect() == ProductionOrderRepository.STATUS_CANCELED
            || productionOrder.getStatusSelect() == ProductionOrderRepository.STATUS_COMPLETED)) {
      statusSelect = ProductionOrderRepository.STATUS_PLANNED;
    }

    productionOrder.setStatusSelect(statusSelect);
    return productionOrderRepo.save(productionOrder);
  }
}
