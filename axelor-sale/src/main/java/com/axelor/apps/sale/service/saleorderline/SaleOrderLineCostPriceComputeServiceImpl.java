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
package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.axelor.common.ObjectUtils;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

public class SaleOrderLineCostPriceComputeServiceImpl
    implements SaleOrderLineCostPriceComputeService {

  protected final AppSaleService appSaleService;
  protected final ProductCompanyService productCompanyService;
  protected final CurrencyScaleService currencyScaleService;
  protected final SaleOrderLineProductService saleOrderLineProductService;
  protected final CurrencyService currencyService;

  @Inject
  public SaleOrderLineCostPriceComputeServiceImpl(
      AppSaleService appSaleService,
      ProductCompanyService productCompanyService,
      CurrencyScaleService currencyScaleService,
      SaleOrderLineProductService saleOrderLineProductService,
      CurrencyService currencyService) {
    this.appSaleService = appSaleService;
    this.productCompanyService = productCompanyService;
    this.currencyScaleService = currencyScaleService;
    this.saleOrderLineProductService = saleOrderLineProductService;
    this.currencyService = currencyService;
  }

  @Override
  public Map<String, Object> computeSubTotalCostPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Product product) throws AxelorException {
    return saleOrderLineProductService.fillCostPrice(saleOrderLine, saleOrder);
  }

  @Override
  public void computeTotalCost(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    Company company = saleOrder.getCompany();
    LocalDate localDate = appSaleService.getTodayDate(saleOrder.getCompany());
    BigDecimal costPrice = BigDecimal.ZERO;
    BigDecimal costTotal = BigDecimal.ZERO;

    if (ObjectUtils.isEmpty(saleOrderLine.getSubSaleOrderLineList())) {
      if (saleOrderLine.getProduct() != null) {
        costPrice =
            (BigDecimal)
                productCompanyService.get(saleOrderLine.getProduct(), "costPrice", company);
        costPrice =
            currencyService.getAmountCurrencyConvertedAtDate(
                company.getCurrency(), saleOrder.getCurrency(), costPrice, localDate);
        costTotal =
            costPrice
                .multiply(saleOrderLine.getQty())
                .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
      }
      saleOrderLine.setCostPrice(costPrice);
      saleOrderLine.setCostTotal(costTotal);
    } else {
      for (SaleOrderLine subLine : saleOrderLine.getSubSaleOrderLineList()) {
        computeTotalCost(saleOrder, subLine);
        costTotal = subLine.getCostTotal().add(costTotal);
      }
    }
    costPrice =
        costTotal.divide(
            saleOrderLine.getQty(),
            appSaleService.getNbDecimalDigitForUnitPrice(),
            RoundingMode.HALF_UP);
    saleOrderLine.setCostTotal(costTotal);
    saleOrderLine.setCostPrice(costPrice);
  }
}
