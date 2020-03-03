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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderMarginServiceImpl implements SaleOrderMarginService {

  protected final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public void computeMarginSaleOrder(SaleOrder saleOrder) {

    BigDecimal accountedRevenue = BigDecimal.ZERO;
    BigDecimal totalCostPrice = BigDecimal.ZERO;
    BigDecimal totalGrossProfit = BigDecimal.ZERO;
    BigDecimal marginRate = BigDecimal.ZERO;
    BigDecimal markup = BigDecimal.ZERO;

    if (saleOrder.getSaleOrderLineList() != null && !saleOrder.getSaleOrderLineList().isEmpty()) {

      for (SaleOrderLine saleOrderLineList : saleOrder.getSaleOrderLineList()) {

        if (saleOrderLineList.getProduct() == null
            || saleOrderLineList.getSubTotalCostPrice().compareTo(BigDecimal.ZERO) == 0
            || saleOrderLineList.getExTaxTotal().compareTo(BigDecimal.ZERO) == 0) {
        } else {

          accountedRevenue = accountedRevenue.add(saleOrderLineList.getCompanyExTaxTotal());
          totalCostPrice = totalCostPrice.add(saleOrderLineList.getSubTotalCostPrice());
          totalGrossProfit = totalGrossProfit.add(saleOrderLineList.getSubTotalGrossMargin());
          marginRate =
              totalGrossProfit
                  .multiply(new BigDecimal(100))
                  .divide(accountedRevenue, RoundingMode.HALF_EVEN);
          markup =
              totalGrossProfit
                  .multiply(new BigDecimal(100))
                  .divide(totalCostPrice, RoundingMode.HALF_EVEN);
        }
      }
    }

    saleOrder.setAccountedRevenue(accountedRevenue);
    saleOrder.setTotalCostPrice(totalCostPrice);
    saleOrder.setTotalGrossMargin(totalGrossProfit);
    saleOrder.setMarginRate(marginRate);
    saleOrder.setMarkup(markup);
  }
}
