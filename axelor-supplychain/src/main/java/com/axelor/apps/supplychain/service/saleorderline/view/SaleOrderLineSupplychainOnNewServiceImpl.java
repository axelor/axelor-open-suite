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
package com.axelor.apps.supplychain.service.saleorderline.view;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineSupplychainOnNewServiceImpl
    implements SaleOrderLineSupplychainOnNewService {

  protected AnalyticAttrsService analyticAttrsService;
  protected SaleOrderLineViewSupplychainService saleOrderLineViewSupplychainService;

  @Inject
  public SaleOrderLineSupplychainOnNewServiceImpl(
      AnalyticAttrsService analyticAttrsService,
      SaleOrderLineViewSupplychainService saleOrderLineViewSupplychainService) {
    this.analyticAttrsService = analyticAttrsService;
    this.saleOrderLineViewSupplychainService = saleOrderLineViewSupplychainService;
  }

  @Override
  public Map<String, Map<String, Object>> getSupplychainOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, saleOrderLineViewSupplychainService.hideSupplychainPanels(saleOrder));
    MapTools.addMap(attrs, saleOrderLineViewSupplychainService.hideDeliveredQty(saleOrder));
    MapTools.addMap(
        attrs, saleOrderLineViewSupplychainService.hideAllocatedQtyBtn(saleOrder, saleOrderLine));
    analyticAttrsService.addAnalyticAxisAttrs(saleOrder.getCompany(), null, attrs);
    MapTools.addMap(
        attrs,
        saleOrderLineViewSupplychainService.setAnalyticDistributionPanelHidden(
            saleOrder, saleOrderLine));
    MapTools.addMap(attrs, saleOrderLineViewSupplychainService.setReservedQtyReadonly(saleOrder));
    return attrs;
  }
}
