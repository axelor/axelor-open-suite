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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.db.mapper.Mapper;
import com.axelor.rpc.Context;
import java.math.BigDecimal;
import java.util.Map;

public class SaleOrderLineServiceImpl implements SaleOrderLineService {

  @Override
  public SaleOrder getSaleOrder(Context context) {

    Context parentContext = context.getParent();

    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    SaleOrder saleOrder = saleOrderLine.getSaleOrder();

    if (parentContext != null && !parentContext.getContextClass().equals(SaleOrder.class)) {
      parentContext = parentContext.getParent();
    }

    if (parentContext != null && parentContext.getContextClass().equals(SaleOrder.class)) {
      saleOrder = parentContext.asType(SaleOrder.class);
    }

    return saleOrder;
  }

  @Override
  public Map<String, Object> emptyLine(SaleOrderLine saleOrderLine) {
    Map<String, Object> newSaleOrderLine = Mapper.toMap(new SaleOrderLine());
    newSaleOrderLine.put("qty", BigDecimal.ZERO);
    newSaleOrderLine.put("id", saleOrderLine.getId());
    newSaleOrderLine.put("version", saleOrderLine.getVersion());
    newSaleOrderLine.put("typeSelect", saleOrderLine.getTypeSelect());
    return newSaleOrderLine;
  }
}
