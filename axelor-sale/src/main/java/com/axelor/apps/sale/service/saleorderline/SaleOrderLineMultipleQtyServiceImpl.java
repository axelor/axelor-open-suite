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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductMultipleQty;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.studio.db.AppSale;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class SaleOrderLineMultipleQtyServiceImpl implements SaleOrderLineMultipleQtyService {

  protected ProductMultipleQtyService productMultipleQtyService;
  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderLineMultipleQtyServiceImpl(
      ProductMultipleQtyService productMultipleQtyService, AppSaleService appSaleService) {
    this.productMultipleQtyService = productMultipleQtyService;
    this.appSaleService = appSaleService;
  }

  @Override
  public String getMultipleQtyErrorMessage(SaleOrderLine saleOrderLine) {

    AppSale appSale = appSaleService.getAppSale();
    Product product = saleOrderLine.getProduct();

    if (product == null || !appSale.getManageMultipleSaleQuantity()) {
      return "";
    }

    BigDecimal qty = saleOrderLine.getQty();
    List<ProductMultipleQty> productMultipleQtyList = product.getSaleProductMultipleQtyList();

    if (appSale.getListDisplayTypeSelect() == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_EDITABLE
        && !productMultipleQtyService.isMultipleQty(qty, productMultipleQtyList)) {
      return productMultipleQtyService.getMultipleQuantityErrorMessage(productMultipleQtyList);
    }
    return "";
  }
}
