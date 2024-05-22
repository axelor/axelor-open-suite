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
package com.axelor.apps.stock.utils;

import com.axelor.apps.stock.db.InventoryLine;

public interface InventoryLineUtilsService {

  /**
   * Will check if the inventory line is present in its stock location as a stockLocationLine. This
   * method will return true if the inventory line product or stock location is null
   *
   * @param inventoryLine
   * @return false if not present in its stock location, else true
   */
  public boolean isPresentInStockLocation(InventoryLine inventoryLine);
}
