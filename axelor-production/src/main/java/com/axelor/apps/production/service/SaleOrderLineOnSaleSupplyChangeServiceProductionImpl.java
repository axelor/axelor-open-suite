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
package com.axelor.apps.production.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.view.SaleOrderLineOnSaleSupplyChangeServiceImpl;
import jakarta.inject.Inject;
import java.util.Map;

public class SaleOrderLineOnSaleSupplyChangeServiceProductionImpl
    extends SaleOrderLineOnSaleSupplyChangeServiceImpl {

  protected SaleOrderLineViewProductionService saleOrderLineViewProductionService;
  protected final AppBaseService appBaseService;

  @Inject
  public SaleOrderLineOnSaleSupplyChangeServiceProductionImpl(
      SaleOrderLineProductSupplychainService saleOrderLineProductSupplychainService,
      SaleOrderLineViewProductionService saleOrderLineViewProductionService,
      AppBaseService appBaseService) {
    super(saleOrderLineProductSupplychainService);
    this.saleOrderLineViewProductionService = saleOrderLineViewProductionService;
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Map<String, Object>> onSaleSupplyChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs =
        super.onSaleSupplyChangeAttrs(saleOrderLine, saleOrder);

    if (!appBaseService.isApp("production")) {
      return attrs;
    }

    attrs.putAll(saleOrderLineViewProductionService.hideBomAndProdProcess(saleOrderLine));
    return attrs;
  }
}
