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
package com.axelor.apps.supplychain.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnLoad;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnNew;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.view.SaleOrderLineSupplychainOnLoadService;
import com.axelor.apps.supplychain.service.saleorderline.view.SaleOrderLineSupplychainOnNewService;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import java.util.Map;

public class SaleOrderLineSupplychainObserver {
  void onSaleOrderLineOnNew(@Observes SaleOrderLineViewOnNew event) throws AxelorException {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineSupplychainOnNewService.class)
            .getSupplychainOnNewAttrs(saleOrderLine, saleOrder));
  }

  void onSaleOrderLineOnLoad(@Observes SaleOrderLineViewOnLoad event) throws AxelorException {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineSupplychainOnLoadService.class)
            .getSupplychainOnLoadAttrs(saleOrderLine, saleOrder));
  }

  void onSaleOrderLineProductOnChange(@Observes SaleOrderLineProductOnChange event)
      throws AxelorException {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Object> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineProductSupplychainService.class)
            .computeProductInformationSupplychain(saleOrderLine, saleOrder));
  }
}
