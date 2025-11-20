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
package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeQtyServiceImpl;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineComputeQtyProductionServiceImpl
    extends SaleOrderLineComputeQtyServiceImpl {

  protected SaleOrderLineProductionService saleOrderLineProductionService;

  @Inject
  public SaleOrderLineComputeQtyProductionServiceImpl(
      SaleOrderLineProductionService saleOrderLineProductionService) {
    this.saleOrderLineProductionService = saleOrderLineProductionService;
  }

  @Override
  public Map<String, Object> initQty(SaleOrderLine saleOrderLine) {
    Map<String, Object> values = super.initQty(saleOrderLine);
    values.put(
        "qtyToProduce",
        saleOrderLineProductionService.computeQtyToProduce(
            saleOrderLine, saleOrderLine.getParentSaleOrderLine()));

    return values;
  }
}
