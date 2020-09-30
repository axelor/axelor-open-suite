/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.repo.ProdProductRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProdProductProductionRepository extends ProdProductRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (!Beans.get(AppProductionService.class).isApp("production")) {

      return super.populate(json, context);
    }

    Object productFromView = json.get("product");
    Object qtyFromView = json.get("qty");
    Object toProduceManufOrderIdFromView;
    if (context == null || context.isEmpty()) {
      toProduceManufOrderIdFromView =
          json.get("toConsumeManufOrder") == null
              ? null
              : ((HashMap<String, Object>) json.get("toConsumeManufOrder")).get("id");
    } else {
      toProduceManufOrderIdFromView = context.get("id");
    }
    if (productFromView == null || qtyFromView == null || toProduceManufOrderIdFromView == null) {
      return super.populate(json, context);
    } else {
      Long productId = (Long) ((HashMap<String, Object>) productFromView).get("id");
      Long toProduceManufOrderId = (Long) toProduceManufOrderIdFromView;
      json.put(
          "$missingQty",
          computeMissingQty(productId, (BigDecimal) qtyFromView, toProduceManufOrderId));
    }
    return super.populate(json, context);
  }

  protected BigDecimal computeMissingQty(
      Long productId, BigDecimal qty, Long toProduceManufOrderId) {
    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForQty();
    if (productId == null || qty == null || toProduceManufOrderId == null) {
      return BigDecimal.ZERO;
    }
    List<BigDecimal> queryResult =
        JPA.em()
            .createQuery(
                "SELECT locationLine.currentQty "
                    + "FROM ManufOrder manufOrder "
                    + "LEFT JOIN StockLocationLine locationLine "
                    + "ON locationLine.stockLocation.id = manufOrder.prodProcess.stockLocation.id "
                    + "WHERE locationLine.product.id = :productId "
                    + "AND manufOrder.id = :manufOrderId",
                BigDecimal.class)
            .setParameter("productId", productId)
            .setParameter("manufOrderId", toProduceManufOrderId)
            .getResultList();
    BigDecimal availableQty;
    if (queryResult.isEmpty()) {
      availableQty = BigDecimal.ZERO;
    } else {
      availableQty = queryResult.get(0);
    }
    return BigDecimal.ZERO.max(qty.subtract(availableQty)).setScale(scale, RoundingMode.HALF_UP);
  }
}
