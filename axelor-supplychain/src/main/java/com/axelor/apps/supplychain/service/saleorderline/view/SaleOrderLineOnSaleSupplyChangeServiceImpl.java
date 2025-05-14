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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineOnSaleSupplyChangeServiceImpl
    implements SaleOrderLineOnSaleSupplyChangeService {

  protected SaleOrderLineProductSupplychainService saleOrderLineProductSupplychainService;

  @Inject
  public SaleOrderLineOnSaleSupplyChangeServiceImpl(
      SaleOrderLineProductSupplychainService saleOrderLineProductSupplychainService) {
    this.saleOrderLineProductSupplychainService = saleOrderLineProductSupplychainService;
  }

  @Override
  public Map<String, Object> onSaleSupplyChangeValues(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(
        saleOrderLineProductSupplychainService.getProductionInformation(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(
        saleOrderLineProductSupplychainService.setSupplierPartnerDefault(saleOrderLine, saleOrder));
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Map<String, Object>> onSaleSupplyChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    return attrs;
  }
}
