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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SaleOrderSplitDummyServiceImpl implements SaleOrderSplitDummyService {

  protected final AppBaseService appBaseService;
  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final SaleOrderSplitService saleOrderSplitService;

  @Inject
  public SaleOrderSplitDummyServiceImpl(
      AppBaseService appBaseService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderSplitService saleOrderSplitService) {
    this.appBaseService = appBaseService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderSplitService = saleOrderSplitService;
  }

  @Override
  public List<Map<String, Object>> getSaleOrderLineMapList(SaleOrder saleOrder) {
    List<Map<String, Object>> saleOrderLineMapList = new ArrayList<>();
    int nbOfDecimalForQty = appBaseService.getNbDecimalDigitForQty();
    int nbOfDecimalForPrice = appBaseService.getNbDecimalDigitForUnitPrice();
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      Map<String, Object> saleOrderLineMap =
          Mapper.toMap(saleOrderLineRepository.copy(saleOrderLine, true));
      saleOrderLineMap.put("$solId", saleOrderLine.getId());
      saleOrderLineMap.put(
          "$qtyToOrderLeft", saleOrderSplitService.getQtyToOrderLeft(saleOrderLine));
      saleOrderLineMap.put("$nbDecimalDigitForUnitPrice", nbOfDecimalForPrice);
      saleOrderLineMap.put("$nbDecimalDigitForQty", nbOfDecimalForQty);
      saleOrderLineMapList.add(saleOrderLineMap);
    }
    return saleOrderLineMapList;
  }
}
