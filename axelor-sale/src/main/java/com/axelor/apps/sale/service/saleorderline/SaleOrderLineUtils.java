/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrderLine;

public class SaleOrderLineUtils {

  private SaleOrderLineUtils() {}

  public static SaleOrderLine getParentSol(SaleOrderLine saleOrderLine) {
    SaleOrderLine parentSaleOrderLine = saleOrderLine.getParentSaleOrderLine();
    if (parentSaleOrderLine != null) {
      return getParentSol(parentSaleOrderLine);
    } else {
      if (saleOrderLine.getSaleOrder() != null) {
        return saleOrderLine;
      }
    }
    return saleOrderLine;
  }
}
