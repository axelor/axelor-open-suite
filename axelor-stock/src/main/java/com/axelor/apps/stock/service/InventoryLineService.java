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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;

public interface InventoryLineService {

  public InventoryLine createInventoryLine(
      Inventory inventory,
      Product product,
      BigDecimal currentQty,
      String rack,
      TrackingNumber trackingNumber)
      throws AxelorException;

  public InventoryLine createInventoryLine(
      Inventory inventory,
      Product product,
      BigDecimal currentQty,
      String rack,
      TrackingNumber trackingNumber,
      BigDecimal realQty,
      String description,
      StockLocation stockLocation,
      StockLocation detailsStockLocation)
      throws AxelorException;

  public InventoryLine updateInventoryLine(InventoryLine inventoryLine, Inventory inventory);

  public InventoryLine compute(InventoryLine inventoryLine, Inventory inventory)
      throws AxelorException;

  public BigDecimal getCurrentQty(StockLocation stockLocation, Product product);

  public void updateInventoryLine(
      InventoryLine inventoryLine, BigDecimal realQty, String description) throws AxelorException;

  public InventoryLine addLine(
      Inventory inventory,
      Product product,
      TrackingNumber trackingNumber,
      String rack,
      BigDecimal realQty)
      throws AxelorException;
}
