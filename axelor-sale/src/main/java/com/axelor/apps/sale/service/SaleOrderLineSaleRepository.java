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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.inject.Beans;
import java.util.Map;

public class SaleOrderLineSaleRepository extends SaleOrderLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put(
        "$nbDecimalDigitForUnitPrice",
        Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice());
    json.put("$nbDecimalDigitForQty", Beans.get(AppBaseService.class).getNbDecimalDigitForQty());

    if (context.get("_model") != null
        && context.get("_model").toString().contains("SaleOrder")
        && context.get("id") != null) {
      Long id = (Long) json.get("id");
      if (id != null) {
        SaleOrderLine saleOrderLine = find(id);
        json.put(
            "$hasWarning",
            saleOrderLine.getSaleOrder() != null
                && (saleOrderLine.getSaleOrder().getStatusSelect()
                        == SaleOrderRepository.STATUS_DRAFT_QUOTATION
                    || (saleOrderLine.getSaleOrder().getStatusSelect()
                            == SaleOrderRepository.STATUS_ORDER_CONFIRMED
                        && saleOrderLine.getSaleOrder().getOrderBeingEdited()))
                && saleOrderLine.getDiscountsNeedReview());
      }
    }
    return super.populate(json, context);
  }
}
