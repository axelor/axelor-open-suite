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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.service.InventoryLineService;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Map;

public class ImportInventoryLine {

  @Inject private InventoryLineRepository inventoryLineRepo;

  @Inject private InventoryLineService inventoryLineService;

  @Inject private TrackingNumberService trackingNumberService;

  @Inject private AppBaseService appBaseService;

  @Transactional(rollbackOn = {Exception.class})
  public Object importInventoryLine(Object bean, Map<String, Object> values)
      throws AxelorException {

    assert bean instanceof InventoryLine;

    InventoryLine inventoryLine = (InventoryLine) bean;

    TrackingNumberConfiguration trackingNumberConfig =
        inventoryLine.getProduct().getTrackingNumberConfiguration();

    BigDecimal qtyByTracking = BigDecimal.ONE;

    BigDecimal realQtyRemaning = inventoryLine.getRealQty();

    inventoryLineService.compute(inventoryLine, inventoryLine.getInventory());

    TrackingNumber trackingNumber;

    if (trackingNumberConfig != null) {

      if (trackingNumberConfig.getGenerateProductionAutoTrackingNbr()) {
        qtyByTracking = trackingNumberConfig.getProductionQtyByTracking();
      } else if (trackingNumberConfig.getGeneratePurchaseAutoTrackingNbr()) {
        qtyByTracking = trackingNumberConfig.getPurchaseQtyByTracking();
      } else {
        qtyByTracking = trackingNumberConfig.getSaleQtyByTracking();
      }

      InventoryLine inventoryLineNew;

      for (int i = 0; i < inventoryLine.getRealQty().intValue(); i += qtyByTracking.intValue()) {

        trackingNumber =
            trackingNumberService.createTrackingNumber(
                inventoryLine.getProduct(),
                inventoryLine.getInventory().getStockLocation().getCompany(),
                appBaseService.getTodayDate(
                    inventoryLine.getInventory().getStockLocation().getCompany()),
                inventoryLine.getInventory().getInventorySeq());

        if (realQtyRemaning.compareTo(qtyByTracking) < 0) {
          trackingNumber.setCounter(realQtyRemaning);
        } else {
          trackingNumber.setCounter(qtyByTracking);
        }

        inventoryLineNew =
            inventoryLineService.createInventoryLine(
                inventoryLine.getInventory(),
                inventoryLine.getProduct(),
                inventoryLine.getCurrentQty(),
                inventoryLine.getRack(),
                trackingNumber,
                null,
                null,
                inventoryLine.getInventory().getStockLocation(),
                null);

        inventoryLineNew.setUnit(inventoryLine.getProduct().getUnit());

        if (realQtyRemaning.compareTo(qtyByTracking) < 0) {
          inventoryLineNew.setRealQty(realQtyRemaning);
        } else {
          inventoryLineNew.setRealQty(qtyByTracking);
          realQtyRemaning = realQtyRemaning.subtract(qtyByTracking);
        }

        inventoryLineRepo.save(inventoryLineNew);
      }
      return null;
    }
    return bean;
  }
}
