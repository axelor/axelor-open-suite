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
package com.axelor.apps.sale.service.event;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineProductOnChange {
  private final SaleOrderLine saleOrderLine;
  private final SaleOrder saleOrder;
  private final Map<String, Object> saleOrderLineMap;

  public SaleOrderLineProductOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    this.saleOrderLine = saleOrderLine;
    this.saleOrder = saleOrder;
    this.saleOrderLineMap = new HashMap<>();
  }

  public SaleOrderLine getSaleOrderLine() {
    return saleOrderLine;
  }

  public SaleOrder getSaleOrder() {
    return saleOrder;
  }

  public Map<String, Object> getSaleOrderLineMap() {
    return saleOrderLineMap;
  }
}
