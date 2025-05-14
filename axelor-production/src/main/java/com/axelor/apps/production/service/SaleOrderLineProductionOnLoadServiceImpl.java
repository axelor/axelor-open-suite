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

import static com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineViewService.HIDDEN_ATTR;

import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineProductionOnLoadServiceImpl
    implements SaleOrderLineProductionOnLoadService {

  protected SaleOrderLineViewProductionService saleOrderLineViewProductionService;
  protected ManufOrderRepository manufOrderRepository;

  @Inject
  public SaleOrderLineProductionOnLoadServiceImpl(
      SaleOrderLineViewProductionService saleOrderLineViewProductionService,
      ManufOrderRepository manufOrderRepository) {
    this.saleOrderLineViewProductionService = saleOrderLineViewProductionService;
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  public Map<String, Map<String, Object>> getProductionOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs =
        saleOrderLineViewProductionService.hideBomAndProdProcess(saleOrderLine);
    MapTools.addMap(attrs, hideQtyProduced(saleOrderLine, saleOrder));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideQtyProduced(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    ManufOrder manufOrder = null;
    if (saleOrderLine.getProduct() != null) {
      manufOrder =
          manufOrderRepository
              .all()
              .filter("self.product.id=:productId and self.saleOrderLine.id=:saleOrderLineId")
              .bind("saleOrderLineId", saleOrderLine.getId())
              .bind("productId", saleOrderLine.getProduct().getId())
              .fetchOne();
    }
    attrs.put(
        "qtyProducedPanel",
        Map.of(
            HIDDEN_ATTR,
            saleOrder.getStatusSelect() < SaleOrderRepository.STATUS_ORDER_CONFIRMED
                || manufOrder == null));
    return attrs;
  }
}
