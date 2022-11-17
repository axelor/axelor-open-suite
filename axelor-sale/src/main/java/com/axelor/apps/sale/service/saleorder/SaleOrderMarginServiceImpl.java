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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderMarginServiceImpl implements SaleOrderMarginService {

  protected final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderMarginServiceImpl(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
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

  public BigDecimal computeRate(BigDecimal saleCostPrice, BigDecimal totalGrossMargin) {
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
