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
package com.axelor.apps.production.service;

import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeServiceImpl;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SubSaleOrderLineComputeServiceProductionImpl
    extends SubSaleOrderLineComputeServiceImpl {
  @Inject
  public SubSaleOrderLineComputeServiceProductionImpl(
      SaleOrderLineComputeService saleOrderLineComputeService,
      AppSaleService appSaleService,
      CurrencyScaleService currencyScaleService) {
    super(saleOrderLineComputeService, appSaleService, currencyScaleService);
  }

  @Override
  protected void computePrices(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    if (CollectionUtils.isEmpty(subSaleOrderLineList)
        && CollectionUtils.isEmpty(saleOrderLineDetailsList)) {
      return;
    }
    saleOrderLine.setPrice(computeTotalPrice(saleOrderLine));
    saleOrderLine.setSubTotalCostPrice(
        currencyScaleService.getCompanyScaledValue(
            saleOrder, computeTotalCostPrice(saleOrderLine)));
  }

  protected BigDecimal computeTotalPrice(SaleOrderLine saleOrderLine) {
    BigDecimal totalPrice = BigDecimal.ZERO;
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(subSaleOrderLineList)) {
      totalPrice =
          totalPrice.add(
              subSaleOrderLineList.stream()
                  .map(SaleOrderLine::getExTaxTotal)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      totalPrice =
          totalPrice.add(
              saleOrderLineDetailsList.stream()
                  .map(SaleOrderLineDetails::getTotalPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    return totalPrice;
  }

  protected BigDecimal computeTotalCostPrice(SaleOrderLine saleOrderLine) {
    BigDecimal totalCostPrice = BigDecimal.ZERO;
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(subSaleOrderLineList)) {
      totalCostPrice =
          totalCostPrice.add(
              subSaleOrderLineList.stream()
                  .map(SaleOrderLine::getSubTotalCostPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      totalCostPrice =
          totalCostPrice.add(
              saleOrderLineDetailsList.stream()
                  .map(SaleOrderLineDetails::getSubTotalCostPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    return totalCostPrice;
  }
}
