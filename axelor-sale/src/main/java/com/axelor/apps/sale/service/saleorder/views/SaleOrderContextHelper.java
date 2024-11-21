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
package com.axelor.apps.sale.service.saleorder.views;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import java.util.LinkedHashMap;

public class SaleOrderContextHelper {
  private SaleOrderContextHelper() {}

  public static SaleOrder getSaleOrder(Context context) {

    SaleOrder saleOrder;
    if (context.get("_saleOrderTemplate") != null) {
      LinkedHashMap<String, Object> saleOrderTemplateContext =
          (LinkedHashMap<String, Object>) context.get("_saleOrderTemplate");
      Integer saleOrderId = (Integer) saleOrderTemplateContext.get("id");
      saleOrder = Beans.get(SaleOrderRepository.class).find(Long.valueOf(saleOrderId));
    } else {
      saleOrder = context.asType(SaleOrder.class);
    }

    return saleOrder;
  }
}
