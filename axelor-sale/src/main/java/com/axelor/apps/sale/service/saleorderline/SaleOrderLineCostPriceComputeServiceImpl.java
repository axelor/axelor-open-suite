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
package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineCostPriceComputeServiceImpl
    implements SaleOrderLineCostPriceComputeService {

  protected final AppSaleService appSaleService;
  protected final ProductCompanyService productCompanyService;
  protected final CurrencyScaleService currencyScaleService;
  protected final SaleOrderLineProductService saleOrderLineProductService;

  @Inject
  public SaleOrderLineCostPriceComputeServiceImpl(
      AppSaleService appSaleService,
      ProductCompanyService productCompanyService,
      CurrencyScaleService currencyScaleService,
      SaleOrderLineProductService saleOrderLineProductService) {
    this.appSaleService = appSaleService;
    this.productCompanyService = productCompanyService;
    this.currencyScaleService = currencyScaleService;
    this.saleOrderLineProductService = saleOrderLineProductService;
  }

  @Override
  public Map<String, Object> computeSubTotalCostPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Product product) throws AxelorException {
    return saleOrderLineProductService.fillCostPrice(saleOrderLine, saleOrder);
  }
}
