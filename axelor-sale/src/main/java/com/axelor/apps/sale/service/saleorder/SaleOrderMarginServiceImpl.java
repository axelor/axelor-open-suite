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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderMarginServiceImpl implements SaleOrderMarginService {

  protected final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected AppSaleService appSaleService;
  protected CurrencyService currencyService;
  protected ProductCompanyService productCompanyService;

  @Inject
  public SaleOrderMarginServiceImpl(
      AppSaleService appSaleService,
      CurrencyService currencyService,
      ProductCompanyService productCompanyService) {
    this.appSaleService = appSaleService;
    this.currencyService = currencyService;
    this.productCompanyService = productCompanyService;
  }

  @Override
  public void computeMarginSaleOrder(SaleOrder saleOrder) {
    BigDecimal accountedRevenue = BigDecimal.ZERO;
    BigDecimal totalCostPrice = BigDecimal.ZERO;
    BigDecimal totalGrossMargin = BigDecimal.ZERO;

    if (saleOrder.getSaleOrderLineList() != null && !saleOrder.getSaleOrderLineList().isEmpty()) {
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        if (saleOrderLine.getProduct() == null
            || (saleOrderLine.getExTaxTotal().compareTo(BigDecimal.ZERO) == 0
                && !appSaleService.getAppSale().getConsiderZeroCost())) {
          continue;
        }
        totalGrossMargin = totalGrossMargin.add(saleOrderLine.getSubTotalGrossMargin());
        totalCostPrice = totalCostPrice.add(saleOrderLine.getSubTotalCostPrice());
        accountedRevenue = accountedRevenue.add(saleOrderLine.getCompanyExTaxTotal());
      }
    }

    setSaleOrderMarginInfo(
        saleOrder,
        accountedRevenue,
        totalCostPrice,
        totalGrossMargin,
        computeRate(accountedRevenue, totalGrossMargin),
        computeRate(totalCostPrice, totalGrossMargin));
  }

  @Override
  public void computeSubMargin(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    Company company = saleOrder.getCompany();
    Product product = saleOrderLine.getProduct();

    BigDecimal exTaxTotal = saleOrderLine.getExTaxTotal();
    BigDecimal subTotalCostPrice = saleOrderLine.getSubTotalCostPrice();
    BigDecimal subTotalGrossMargin = BigDecimal.ZERO;
    BigDecimal subMarginRate = BigDecimal.ZERO;
    BigDecimal totalWT =
        currencyService.getAmountCurrencyConvertedAtDate(
            saleOrder.getCurrency(), company.getCurrency(), exTaxTotal, null);

    if (product != null
        && exTaxTotal.compareTo(BigDecimal.ZERO) != 0
        && subTotalCostPrice.compareTo(BigDecimal.ZERO) != 0) {
      subTotalGrossMargin = totalWT.subtract(subTotalCostPrice);
      subMarginRate = computeRate(totalWT, subTotalGrossMargin);
    }

    if (appSaleService.getAppSale().getConsiderZeroCost()
        && (exTaxTotal.compareTo(BigDecimal.ZERO) == 0
            || subTotalCostPrice.compareTo(BigDecimal.ZERO) == 0)) {
      subTotalGrossMargin = exTaxTotal.subtract(subTotalCostPrice);
      subMarginRate = computeRate(exTaxTotal, subTotalGrossMargin);
    }

    BigDecimal subMarkup = computeRate(subTotalCostPrice, subTotalGrossMargin);
    setSaleOrderLineMarginInfo(saleOrderLine, subTotalGrossMargin, subMarginRate, subMarkup);
  }

  @Override
  public Map<String, BigDecimal> getSaleOrderLineComputedMarginInfo(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException {
    HashMap<String, BigDecimal> map = new HashMap<>();
    computeSubMargin(saleOrder, saleOrderLine);
    map.put("subTotalGrossMargin", saleOrderLine.getSubTotalGrossMargin());
    map.put("subMarginRate", saleOrderLine.getSubMarginRate());
    map.put("subTotalMarkup", saleOrderLine.getSubTotalMarkup());
    return map;
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

  protected void setSaleOrderLineMarginInfo(
      SaleOrderLine saleOrderLine,
      BigDecimal subTotalGrossMargin,
      BigDecimal subMarginRate,
      BigDecimal subTotalMarkup) {
    saleOrderLine.setSubTotalGrossMargin(subTotalGrossMargin);
    saleOrderLine.setSubMarginRate(subMarginRate);
    saleOrderLine.setSubTotalMarkup(subTotalMarkup);
  }

  protected void setSaleOrderMarginInfo(
      SaleOrder saleOrder,
      BigDecimal accountedRevenue,
      BigDecimal totalCostPrice,
      BigDecimal totalGrossMargin,
      BigDecimal marginRate,
      BigDecimal markup) {
    saleOrder.setAccountedRevenue(accountedRevenue);
    saleOrder.setTotalCostPrice(totalCostPrice);
    saleOrder.setTotalGrossMargin(totalGrossMargin);
    saleOrder.setMarginRate(marginRate);
    saleOrder.setMarkup(markup);
  }
}
