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
package com.axelor.apps.production.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.service.SaleOrderLineProductProductionService;
import com.axelor.apps.production.service.SaleOrderLineViewProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnLoad;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnNew;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import java.util.Map;

public class SaleOrderLineProductionObserver {
  void onSaleOrderLineOnNew(@Observes SaleOrderLineViewOnNew event) {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    SaleOrderLineViewProductionService saleOrderLineViewProductionService =
        Beans.get(SaleOrderLineViewProductionService.class);
    saleOrderLineMap.putAll(
        saleOrderLineViewProductionService.hideBomAndProdProcess(saleOrderLine));
    saleOrderLineMap.putAll(saleOrderLineViewProductionService.getSolDetailsScale());
  }

  void onSaleOrderLineOnLoad(@Observes SaleOrderLineViewOnLoad event) {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    SaleOrderLineViewProductionService saleOrderLineViewProductionService =
        Beans.get(SaleOrderLineViewProductionService.class);
    saleOrderLineMap.putAll(
        saleOrderLineViewProductionService.hideBomAndProdProcess(saleOrderLine));
    saleOrderLineMap.putAll(saleOrderLineViewProductionService.getSolDetailsScale());
  }

  void onSaleOrderLineProductOnChange(@Observes SaleOrderLineProductOnChange event)
      throws AxelorException {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Object> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineProductProductionService.class)
            .computeProductInformationProduction(saleOrderLine, saleOrder));
  }
}
