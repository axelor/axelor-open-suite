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
package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.interfaces.MarginLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class MarginComputeServiceImpl implements MarginComputeService {

  protected final AppSaleService appSaleService;
  protected final CurrencyService currencyService;
  protected final CurrencyScaleService currencyScaleService;

  @Inject
  public MarginComputeServiceImpl(
      AppSaleService appSaleService,
      CurrencyService currencyService,
      CurrencyScaleService currencyScaleService) {
    this.appSaleService = appSaleService;
    this.currencyService = currencyService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public Map<String, BigDecimal> getComputedMarginInfo(
      SaleOrder saleOrder, MarginLine marginLine, BigDecimal totalPrice) throws AxelorException {
    HashMap<String, BigDecimal> map = new HashMap<>();
    computeSubMargin(saleOrder, marginLine, totalPrice);
    map.put("subTotalGrossMargin", marginLine.getSubTotalGrossMargin());
    map.put("subMarginRate", marginLine.getSubMarginRate());
    map.put("subTotalMarkup", marginLine.getSubTotalMarkup());
    return map;
  }

  @Override
  public void computeSubMargin(SaleOrder saleOrder, MarginLine marginLine, BigDecimal totalPrice)
      throws AxelorException {

    Company company = saleOrder.getCompany();
    BigDecimal subTotalCostPrice = marginLine.getSubTotalCostPrice();
    BigDecimal subTotalGrossMargin = BigDecimal.ZERO;
    BigDecimal subMarginRate = BigDecimal.ZERO;
    BigDecimal totalWT =
        currencyService.getAmountCurrencyConvertedAtDate(
            saleOrder.getCurrency(),
            company != null ? company.getCurrency() : null,
            totalPrice,
            null);

    if (totalPrice.compareTo(BigDecimal.ZERO) != 0
        && subTotalCostPrice.compareTo(BigDecimal.ZERO) != 0) {
      subTotalGrossMargin =
          currencyScaleService.getCompanyScaledValue(
              saleOrder, totalWT.subtract(subTotalCostPrice));
      subMarginRate = computeRate(totalWT, subTotalGrossMargin);
    }

    if (appSaleService.getAppSale().getConsiderZeroCost()
        && (totalPrice.compareTo(BigDecimal.ZERO) == 0
            || subTotalCostPrice.compareTo(BigDecimal.ZERO) == 0)) {
      subTotalGrossMargin =
          currencyScaleService.getCompanyScaledValue(
              saleOrder, totalPrice.subtract(subTotalCostPrice));
      subMarginRate = computeRate(totalPrice, subTotalGrossMargin);
    }

    BigDecimal subMarkup = computeRate(subTotalCostPrice, subTotalGrossMargin);
    setMarginInfo(marginLine, subTotalGrossMargin, subMarginRate, subMarkup);
  }

  protected void setMarginInfo(
      MarginLine marginLine,
      BigDecimal subTotalGrossMargin,
      BigDecimal subMarginRate,
      BigDecimal subTotalMarkup) {
    marginLine.setSubTotalGrossMargin(subTotalGrossMargin);
    marginLine.setSubMarginRate(subMarginRate);
    marginLine.setSubTotalMarkup(subTotalMarkup);
  }

  protected BigDecimal computeRate(BigDecimal saleCostPrice, BigDecimal totalGrossMargin) {
    BigDecimal rate = BigDecimal.ZERO;
    if (saleCostPrice.compareTo(BigDecimal.ZERO) != 0) {
      rate =
          totalGrossMargin
              .multiply(new BigDecimal(100))
              .divide(
                  saleCostPrice, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    }
    return rate;
  }
}
