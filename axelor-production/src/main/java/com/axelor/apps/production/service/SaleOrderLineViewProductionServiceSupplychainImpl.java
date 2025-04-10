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

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineViewServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorderline.view.SaleOrderLineViewSupplychainService;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineViewProductionServiceSupplychainImpl
    extends SaleOrderLineViewServiceSupplychainImpl {

  protected SaleOrderLineViewProductionService saleOrderLineViewProductionService;

  @Inject
  public SaleOrderLineViewProductionServiceSupplychainImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      ProductMultipleQtyService productMultipleQtyService,
      AnalyticAttrsService analyticAttrsService,
      SaleOrderLineViewSupplychainService saleOrderLineViewSupplychainService,
      SaleOrderLineViewProductionService saleOrderLineViewProductionService) {
    super(
        appBaseService,
        appSaleService,
        productMultipleQtyService,
        analyticAttrsService,
        saleOrderLineViewSupplychainService);
    this.saleOrderLineViewProductionService = saleOrderLineViewProductionService;
  }

  @Override
  public Map<String, Map<String, Object>> getProductOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs =
        super.getProductOnChangeAttrs(saleOrderLine, saleOrder);
    attrs.putAll(saleOrderLineViewProductionService.hideBomAndProdProcess(saleOrderLine));
    return attrs;
  }
}
