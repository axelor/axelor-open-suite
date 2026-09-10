/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import java.util.List;

public interface ProductMergeStockService {

  /**
   * Returns the message of every stock condition blocking the merge. An empty list means the stock
   * of the absorbed product can be transferred.
   */
  List<String> getStockBlockingChecks(Product absorbedProduct, Product keptProduct);

  /**
   * Transfers the stock of the absorbed product to the kept product: the tracking numbers are
   * moved, the quantities of every stock location are added on the kept product and set to zero on
   * the absorbed product, then the reserved quantities and the average price are computed again.
   *
   * @throws AxelorException if a stock location holds the two products with a different unit, their
   *     quantities cannot be added
   */
  void transferStock(Product absorbedProduct, Product keptProduct, ProductMergeResult result)
      throws AxelorException;
}
