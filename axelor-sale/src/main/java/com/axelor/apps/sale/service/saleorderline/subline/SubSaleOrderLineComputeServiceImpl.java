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
package com.axelor.apps.sale.service.saleorderline.subline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.axelor.studio.db.AppSale;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SubSaleOrderLineComputeServiceImpl implements SubSaleOrderLineComputeService {

  protected final SaleOrderLineComputeService saleOrderLineComputeService;
  protected final AppSaleService appSaleService;
  protected final CurrencyScaleService currencyScaleService;
  protected final SaleOrderLinePriceService saleOrderLinePriceService;
  protected final SaleOrderLineProductService saleOrderLineProductService;

  @Inject
  public SubSaleOrderLineComputeServiceImpl(
      SaleOrderLineComputeService saleOrderLineComputeService,
      AppSaleService appSaleService,
      CurrencyScaleService currencyScaleService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      SaleOrderLineProductService saleOrderLineProductService) {
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.appSaleService = appSaleService;
    this.currencyScaleService = currencyScaleService;
    this.saleOrderLinePriceService = saleOrderLinePriceService;
    this.saleOrderLineProductService = saleOrderLineProductService;
  }

  @Override
  public void computeSumSubLineList(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    AppSale appSale = appSaleService.getAppSale();
    if (appSale.getListDisplayTypeSelect() == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI) {
      if (appSale.getIsSOLPriceTotalOfSubLines()) {
        if (CollectionUtils.isNotEmpty(subSaleOrderLineList)) {
          for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
            computeSumSubLineList(subSaleOrderLine, saleOrder);
          }
          computePrices(saleOrderLine, saleOrder);
        }
      } else {
        saleOrderLineProductService.fillPrice(saleOrderLine, saleOrder);
      }
    }
    saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine);
  }

  protected void computePrices(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (CollectionUtils.isEmpty(subSaleOrderLineList)) {
      return;
    }
    saleOrderLine.setPrice(
        subSaleOrderLineList.stream()
            .map(SaleOrderLine::getExTaxTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    saleOrderLine.setInTaxPrice(
        subSaleOrderLineList.stream()
            .map(SaleOrderLine::getInTaxTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    saleOrderLine.setSubTotalCostPrice(
        currencyScaleService.getCompanyScaledValue(
            saleOrder,
            subSaleOrderLineList.stream()
                .map(SaleOrderLine::getSubTotalCostPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
  }

  @Override
  public void updateSubSaleOrderLineList(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(saleOrderLine.getSubSaleOrderLineList())) {
      for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
        if (subSaleOrderLine.getProduct() != null) {
          if (!saleOrder.getTemplate()) {
            saleOrderLinePriceService.resetPrice(subSaleOrderLine);
          }
          saleOrderLineProductService.fillPrice(subSaleOrderLine, saleOrder);
          saleOrderLineComputeService.computeValues(saleOrder, subSaleOrderLine);
          updateSubSaleOrderLineList(subSaleOrderLine, saleOrder);
        }
      }
    }
  }
}
