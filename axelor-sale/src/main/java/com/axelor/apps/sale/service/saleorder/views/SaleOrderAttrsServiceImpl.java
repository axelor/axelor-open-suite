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
package com.axelor.apps.sale.service.saleorder.views;

import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderGlobalDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderAttrsServiceImpl implements SaleOrderAttrsService {

  protected CurrencyScaleService currencyScaleService;
  protected SaleOrderService saleOrderService;
  protected SaleOrderGlobalDiscountService saleOrderGlobalDiscountService;
  protected SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public SaleOrderAttrsServiceImpl(
      CurrencyScaleService currencyScaleService,
      SaleOrderService saleOrderService,
      SaleOrderGlobalDiscountService saleOrderGlobalDiscountService,
      SaleOrderLineRepository saleOrderLineRepository) {
    this.currencyScaleService = currencyScaleService;
    this.saleOrderService = saleOrderService;
    this.saleOrderGlobalDiscountService = saleOrderGlobalDiscountService;
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void setSaleOrderLineScale(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    int currencyScale = currencyScaleService.getScale(saleOrder);

    this.addAttr("saleOrderLineList.exTaxTotal", "scale", currencyScale, attrsMap);
    this.addAttr("saleOrderLineList.inTaxTotal", "scale", currencyScale, attrsMap);
  }

  @Override
  public void setSaleOrderLineTaxScale(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    int currencyScale = currencyScaleService.getScale(saleOrder);

    this.addAttr("saleOrderLineTaxList.inTaxTotal", "scale", currencyScale, attrsMap);
    this.addAttr("saleOrderLineTaxList.exTaxBase", "scale", currencyScale, attrsMap);
    this.addAttr("saleOrderLineTaxList.taxTotal", "scale", currencyScale, attrsMap);
  }

  @Override
  public void addIncotermRequired(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr("incoterm", "required", saleOrderService.isIncotermRequired(saleOrder), attrsMap);
  }

  @Override
  public void setSaleOrderGlobalDiscountDummies(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder == null) {
      return;
    }
    attrsMap.putAll(saleOrderGlobalDiscountService.setDiscountDummies(saleOrder));
  }

  @Override
  public void setDiscountsNeedReview(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrder == null) {
      return;
    }
    boolean isEditableStatus =
        saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_DRAFT_QUOTATION
            || (Boolean.TRUE.equals(saleOrder.getOrderBeingEdited())
                && saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    boolean discountsNeedReview =
        isEditableStatus
            && saleOrder.getId() != null
            && saleOrderLineRepository
                    .all()
                    .filter("self.saleOrder.id = :id AND self.discountsNeedReview = true")
                    .bind("id", saleOrder.getId())
                    .fetchOne()
                != null;
    addAttr("$discountsNeedReview", "value", discountsNeedReview, attrsMap);
  }
}
