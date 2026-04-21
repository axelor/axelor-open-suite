/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.db.repo;

import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Map;

public class SaleOrderLineSaleRepository extends SaleOrderLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put(
        "$nbDecimalDigitForUnitPrice",
        Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice());
    json.put("$nbDecimalDigitForQty", Beans.get(AppBaseService.class).getNbDecimalDigitForQty());
    json.put(
        "$computeWithSOL",
        Beans.get(AppSaleService.class).getAppSale().getIsSOLPriceTotalOfSubLines());

    if (context.get("_model") != null
        && (context.get("_model").equals(SaleOrder.class.getName())
            || context.get("_model").equals(SaleOrderLine.class.getName()))
        && (context.get("id") != null || context.get("_field_ids") != null)) {
      Long id = (Long) json.get("id");
      if (id != null) {
        BigDecimal qty = (BigDecimal) json.getOrDefault("qty", BigDecimal.ZERO);
        BigDecimal orderedQty = (BigDecimal) json.getOrDefault("orderedQty", BigDecimal.ZERO);
        json.put("$qtyToOrderLeft", qty.subtract(orderedQty));

        SaleOrder directSaleOrder = getDirectSaleOrder(json, context);

        json.put(
            "$hasWarning",
            directSaleOrder != null
                && (directSaleOrder.getStatusSelect() == SaleOrderRepository.STATUS_DRAFT_QUOTATION
                    || (directSaleOrder.getStatusSelect()
                            == SaleOrderRepository.STATUS_ORDER_CONFIRMED
                        && directSaleOrder.getOrderBeingEdited()))
                && Boolean.TRUE.equals(json.get("discountsNeedReview")));

        SaleOrder currencySaleOrder =
            directSaleOrder != null ? directSaleOrder : getOldVersionSaleOrder(json);
        json.put(
            "$currencyNumberOfDecimals",
            Beans.get(CurrencyScaleService.class).getScale(currencySaleOrder));
      }
    }
    return super.populate(json, context);
  }

  @SuppressWarnings("unchecked")
  protected SaleOrder getDirectSaleOrder(Map<String, Object> json, Map<String, Object> context) {
    if (SaleOrder.class.getName().equals(context.get("_model")) && context.get("id") != null) {
      return Beans.get(SaleOrderRepository.class).find((Long) context.get("id"));
    }
    Map<String, Object> saleOrderMap = (Map<String, Object>) json.get("saleOrder");
    if (saleOrderMap != null && saleOrderMap.get("id") != null) {
      return Beans.get(SaleOrderRepository.class).find((Long) saleOrderMap.get("id"));
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  protected SaleOrder getOldVersionSaleOrder(Map<String, Object> json) {
    Map<String, Object> oldVersionMap = (Map<String, Object>) json.get("oldVersionSaleOrder");
    if (oldVersionMap != null && oldVersionMap.get("id") != null) {
      return Beans.get(SaleOrderRepository.class).find((Long) oldVersionMap.get("id"));
    }
    return null;
  }

  @Override
  public SaleOrderLine copy(SaleOrderLine entity, boolean deep) {
    SaleOrderLine copy = super.copy(entity, deep);
    copy.setConfigurator(null);
    copy.setDeliveredQty(BigDecimal.ZERO);

    return copy;
  }
}
