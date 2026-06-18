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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SaleOrderLineQtyToDeliverService {

  /**
   * Initialize qtyToDeliver recursively for all lines and their sub-lines using a top-down
   * cumulative product of parent quantities. Called at order confirmation.
   *
   * @param lines root-level sale order lines
   */
  void initQtyToDeliverForAll(List<SaleOrderLine> lines);

  /**
   * Initialize qtyToDeliver recursively for all sale order lines if the company is configured to
   * generate stock moves only for managed lines. No-op otherwise.
   *
   * @param saleOrder the sale order whose lines must be recomputed
   */
  void initQtyToDeliverForAllIfManagedLines(SaleOrder saleOrder);
}
