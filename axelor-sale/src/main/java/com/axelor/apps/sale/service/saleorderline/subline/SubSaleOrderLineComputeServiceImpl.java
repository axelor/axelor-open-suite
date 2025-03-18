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
package com.axelor.apps.sale.service.saleorderline.subline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SubSaleOrderLineComputeServiceImpl implements SubSaleOrderLineComputeService {

  protected final SaleOrderLineComputeService saleOrderLineComputeService;
  protected final AppSaleService appSaleService;
  protected final CurrencyScaleService currencyScaleService;

  @Inject
  public SubSaleOrderLineComputeServiceImpl(
      SaleOrderLineComputeService saleOrderLineComputeService,
      AppSaleService appSaleService,
      CurrencyScaleService currencyScaleService) {
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.appSaleService = appSaleService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public void computeSumSubLineList(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (appSaleService.getAppSale().getIsSOLPriceTotalOfSubLines()) {
      if (CollectionUtils.isNotEmpty(subSaleOrderLineList)) {
        for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
          computeSumSubLineList(subSaleOrderLine, saleOrder);
        }
      }
      computePrices(saleOrderLine, saleOrder);
    }
    saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine);
  }

  protected void computePrices(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    saleOrderLine.setPrice(
        subSaleOrderLineList.stream()
            .map(SaleOrderLine::getExTaxTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    saleOrderLine.setSubTotalCostPrice(
        currencyScaleService.getCompanyScaledValue(
            saleOrder,
            subSaleOrderLineList.stream()
                .map(SaleOrderLine::getSubTotalCostPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
  }
}
