/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import java.util.Map;

public class SaleOrderLineSaleRepository extends SaleOrderLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
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
