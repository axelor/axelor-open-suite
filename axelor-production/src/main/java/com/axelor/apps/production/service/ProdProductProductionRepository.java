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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.repo.ProdProductRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProdProductProductionRepository extends ProdProductRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (!Beans.get(AppProductionService.class).isApp("production")) {

      return super.populate(json, context);
    }

    Object productFromView = json.get("product");
    Object qtyFromView = json.get("qty");
    Unit unit = Mapper.toBean(Unit.class, (HashMap<String, Object>) json.get("unit"));
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
      try {
        json.put(
            "$missingQty",
            computeMissingQty(productId, (BigDecimal) qtyFromView, toProduceManufOrderId, unit));
      } catch (Exception e) {
        TraceBackService.trace(e);
      }
    }
    return super.populate(json, context);
  }

  public BigDecimal computeMissingQty(
      Long productId, BigDecimal qty, Long toProduceManufOrderId, Unit targetUnit) {
    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForQty();
    if (productId == null || qty == null || toProduceManufOrderId == null) {
      return BigDecimal.ZERO;
    }
    List<Object[]> queryResult =
        JPA.em()
            .createQuery(
                "SELECT locationLine.currentQty, locationLine.unit.id "
                    + "FROM ManufOrder manufOrder "
                    + "LEFT JOIN StockLocationLine locationLine "
                    + "ON locationLine.stockLocation.id = manufOrder.prodProcess.stockLocation.id "
                    + "WHERE locationLine.product.id = :productId "
                    + "AND manufOrder.id = :manufOrderId")
            .setParameter("productId", productId)
            .setParameter("manufOrderId", toProduceManufOrderId)
            .getResultList();

    BigDecimal availableQty = BigDecimal.ZERO;

    if (!queryResult.isEmpty()) {
      try {
        Object[] resultTab = queryResult.get(0);
        BigDecimal availableQtyInLocationUnit =
            Optional.ofNullable(resultTab[0])
                .map(currentQtyObj -> new BigDecimal(currentQtyObj.toString()))
                .orElse(BigDecimal.ZERO);
        Unit locationUnit =
            Optional.ofNullable(resultTab[1])
                .map(
                    unitObj ->
                        Beans.get(UnitRepository.class).find(Long.valueOf(unitObj.toString())))
                .orElse(null);
        if (locationUnit != null) {
          availableQty =
              Beans.get(UnitConversionService.class)
                  .convert(locationUnit, targetUnit, availableQtyInLocationUnit, scale, null);
        }

      } catch (Exception e) {
        TraceBackService.trace(e);
      }
    }
    return BigDecimal.ZERO.max(qty.subtract(availableQty)).setScale(scale, RoundingMode.HALF_UP);
  }
}
