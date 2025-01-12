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

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderAttrsServiceImpl implements SaleOrderAttrsService {

  protected CurrencyScaleService currencyScaleService;
  protected SaleOrderService saleOrderService;
  protected SaleOrderDiscountService saleOrderDiscountService;

  @Inject
  public SaleOrderAttrsServiceImpl(
      CurrencyScaleService currencyScaleService,
      SaleOrderService saleOrderService,
      SaleOrderDiscountService saleOrderDiscountService) {
    this.currencyScaleService = currencyScaleService;
    this.saleOrderService = saleOrderService;
    this.saleOrderDiscountService = saleOrderDiscountService;
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
    switch (saleOrder.getDiscountTypeSelect()) {
      case PriceListLineRepository.AMOUNT_TYPE_PERCENT:
        setSaleOrderPercentageGlobalDiscountDummies(saleOrder, attrsMap);
        break;
      case PriceListLineRepository.AMOUNT_TYPE_FIXED:
        setSaleOrderFixedGlobalDiscountDummies(saleOrder, attrsMap);
        break;
    }
  }

  protected void setSaleOrderPercentageGlobalDiscountDummies(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    Currency currency = saleOrder.getCurrency();
    if (currency == null) {
      return;
    }
    this.addAttr("discountCurrency", "value", "%", attrsMap);
    this.addAttr("discountScale", "value", 2, attrsMap);
  }

  protected void setSaleOrderFixedGlobalDiscountDummies(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap) {
    Currency currency = saleOrder.getCurrency();
    if (currency == null) {
      return;
    }
    this.addAttr("discountCurrency", "value", currency.getSymbol(), attrsMap);
    this.addAttr("discountScale", "value", currency.getNumberOfDecimals(), attrsMap);
  }
}
