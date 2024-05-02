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
package com.axelor.apps.web;

import com.axelor.apps.base.db.ProductType;
import com.axelor.apps.sale.db.PriceStudy;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class SaleOrderController {

  public void populatePriceStudyList(ActionRequest request, ActionResponse response) {

    SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
    populatePriceStudyList(saleOrder);
    response.setValues(saleOrder);
  }

  protected void populatePriceStudyList(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return;
    }
    if (CollectionUtils.isNotEmpty(saleOrder.getPriceStudyList())) {
      saleOrder.getPriceStudyList().clear();
    } else {
      saleOrder.setPriceStudyList(new ArrayList<>());
    }

    Map<ProductType, List<SaleOrderLine>> groupedByProductType =
        saleOrderLineList.stream()
            .collect(Collectors.groupingBy(line -> line.getProduct().getProductType()));

    groupedByProductType.forEach(
        (productType, lines) -> {
          BigDecimal sumOfPurchasePrices =
              lines.stream()
                  .map(SaleOrderLine::getPurchasePrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
          BigDecimal sumOfCostPrices =
              lines.stream()
                  .map(SaleOrderLine::getCostPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);
          BigDecimal sumOfPrices =
              lines.stream().map(SaleOrderLine::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
          BigDecimal averageFG = BigDecimal.ZERO;
          BigDecimal averageGrossMargin = BigDecimal.ZERO;
          if (!lines.isEmpty()) {
            averageFG =
                lines.stream()
                    .map(SaleOrderLine::getGeneralExpenses)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(lines.size()), 2, BigDecimal.ROUND_HALF_EVEN);
            averageGrossMargin =
                lines.stream()
                    .map(SaleOrderLine::getGrossMarging)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(lines.size()), 2, BigDecimal.ROUND_HALF_EVEN);
          }

          PriceStudy newPriceStudy = new PriceStudy();
          newPriceStudy.setCostPrice(sumOfCostPrices);
          newPriceStudy.setPurchasePrice(sumOfPurchasePrices);
          newPriceStudy.setPrice(sumOfPrices);
          newPriceStudy.setAverageFG(averageFG);
          newPriceStudy.setAverageGrossMarge(averageGrossMargin);
          newPriceStudy.setProductType(productType);

          saleOrder.addPriceStudyListItem(newPriceStudy);
        });
  }
}
