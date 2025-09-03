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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.MarginComputeService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineUtils;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineDetailsServiceImpl implements SaleOrderLineDetailsService {
  protected final ProductCompanyService productCompanyService;
  protected final AppSaleService appSaleService;
  protected final SaleOrderLineProductService saleOrderLineProductService;
  protected final MarginComputeService marginComputeService;
  protected final SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService;

  @Inject
  public SaleOrderLineDetailsServiceImpl(
      ProductCompanyService productCompanyService,
      AppSaleService appSaleService,
      SaleOrderLineProductService saleOrderLineProductService,
      MarginComputeService marginComputeService,
      SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService) {
    this.productCompanyService = productCompanyService;
    this.appSaleService = appSaleService;
    this.saleOrderLineProductService = saleOrderLineProductService;
    this.marginComputeService = marginComputeService;
    this.saleOrderLineDetailsPriceService = saleOrderLineDetailsPriceService;
  }

  @Override
  public Map<String, Object> productOnChange(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> lineMap = new HashMap<>();
    Company company = saleOrder.getCompany();
    Product product = saleOrderLineDetails.getProduct();
    if (product == null) {
      return lineMap;
    }
    BigDecimal price = (BigDecimal) productCompanyService.get(product, "salePrice", company);
    BigDecimal costPrice = (BigDecimal) productCompanyService.get(product, "costPrice", company);
    BigDecimal totalPrice =
        price
            .multiply(saleOrderLineDetails.getQty())
            .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    BigDecimal totalCostPrice =
        costPrice
            .multiply(saleOrderLineDetails.getQty())
            .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);

    setLineInfo(
        saleOrderLineDetails,
        costPrice.setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP),
        price,
        totalPrice,
        totalCostPrice,
        product);
    lineMap.putAll(saleOrderLineDetailsPriceService.computeMarginCoef(saleOrderLineDetails));
    setMapInfo(saleOrderLineDetails, saleOrder, lineMap);
    return lineMap;
  }

  protected void setLineInfo(
      SaleOrderLineDetails saleOrderLineDetails,
      BigDecimal costPrice,
      BigDecimal price,
      BigDecimal totalPrice,
      BigDecimal totalCostPrice,
      Product product) {
    saleOrderLineDetails.setCostPrice(costPrice);
    saleOrderLineDetails.setPrice(price);
    saleOrderLineDetails.setTotalPrice(totalPrice);
    saleOrderLineDetails.setSubTotalCostPrice(totalCostPrice);
    saleOrderLineDetails.setTitle(product.getName());
    saleOrderLineDetails.setUnit(saleOrderLineProductService.getSaleUnit(product));
  }

  protected void setMapInfo(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder, Map<String, Object> lineMap)
      throws AxelorException {
    lineMap.putAll(
        marginComputeService.getComputedMarginInfo(
            saleOrder, saleOrderLineDetails, saleOrderLineDetails.getTotalPrice()));
    lineMap.put("price", saleOrderLineDetails.getPrice());
    lineMap.put("totalPrice", saleOrderLineDetails.getTotalPrice());
    lineMap.put("title", saleOrderLineDetails.getTitle());
    lineMap.put("unit", saleOrderLineDetails.getUnit());
    lineMap.put("subTotalCostPrice", saleOrderLineDetails.getSubTotalCostPrice());
    lineMap.put("costPrice", saleOrderLineDetails.getCostPrice());
  }

  @Override
  public SaleOrder getParentSaleOrder(SaleOrderLineDetails saleOrderLineDetails) {
    return SaleOrderLineUtils.getParentSol(saleOrderLineDetails.getSaleOrderLine()).getSaleOrder();
  }
}
