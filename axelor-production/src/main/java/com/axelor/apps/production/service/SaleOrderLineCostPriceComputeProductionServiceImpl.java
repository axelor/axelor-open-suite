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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCostPriceComputeServiceImpl;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineCostPriceComputeProductionServiceImpl
    extends SaleOrderLineCostPriceComputeServiceImpl {

  @Inject
  public SaleOrderLineCostPriceComputeProductionServiceImpl(
      AppSaleService appSaleService,
      ProductCompanyService productCompanyService,
      CurrencyScaleService currencyScaleService) {
    super(appSaleService, productCompanyService, currencyScaleService);
  }

  @Override
  public Map<String, Object> computeSubTotalCostPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Product product) throws AxelorException {
    Map<String, Object> map = new HashMap<>();
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    if (appSaleService.getAppSale().getListDisplayTypeSelect()
            != AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI
        || (CollectionUtils.isEmpty(subSaleOrderLineList)
            && CollectionUtils.isEmpty(saleOrderLineDetailsList))) {
      return super.computeSubTotalCostPrice(saleOrder, saleOrderLine, product);
    }
    BigDecimal costPriceTotal = BigDecimal.ZERO;
    if (CollectionUtils.isNotEmpty(subSaleOrderLineList)) {
      costPriceTotal =
          costPriceTotal.add(
              subSaleOrderLineList.stream()
                  .map(SaleOrderLine::getSubTotalCostPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      costPriceTotal =
          costPriceTotal.add(
              saleOrderLineDetailsList.stream()
                  .map(SaleOrderLineDetails::getSubTotalCostPrice)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    saleOrderLine.setSubTotalCostPrice(
        currencyScaleService.getCompanyScaledValue(
            saleOrder, costPriceTotal.multiply(saleOrderLine.getQty())));
    map.put("subTotalCostPrice", saleOrderLine.getSubTotalCostPrice());
    return map;
  }
}
