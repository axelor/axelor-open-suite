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
package com.axelor.apps.sale.service.saleorder.views;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderGroupServiceImpl implements SaleOrderGroupService {

  protected SaleOrderAttrsService saleOrderAttrsService;

  @Inject
  public SaleOrderGroupServiceImpl(SaleOrderAttrsService saleOrderAttrsService) {
    this.saleOrderAttrsService = saleOrderAttrsService;
  }

  @Override
  public Map<String, Map<String, Object>> onChangeSaleOrderLine(SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (saleOrder != null && ObjectUtils.notEmpty(saleOrder.getSaleOrderLineList())) {
      saleOrderAttrsService.addIncotermRequired(saleOrder, attrsMap);
      saleOrderAttrsService.setSaleOrderLineScale(saleOrder, attrsMap);
      saleOrderAttrsService.setSaleOrderLineTaxScale(saleOrder, attrsMap);
      saleOrderAttrsService.setSaleOrderGlobalDiscountDummies(saleOrder, attrsMap);
    }

    return attrsMap;
  }
}
