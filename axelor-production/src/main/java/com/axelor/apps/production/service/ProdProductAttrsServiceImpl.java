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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceService;
import com.google.inject.Inject;
import java.util.Objects;

public class ProdProductAttrsServiceImpl implements ProdProductAttrsService {

  protected final StockConfigProductionService stockConfigProductionService;
  protected final ManufOrderOutsourceService manufOrderOutsourceService;

  @Inject
  public ProdProductAttrsServiceImpl(
      StockConfigProductionService stockConfigProductionService,
      ManufOrderOutsourceService manufOrderOutsourceService) {
    this.stockConfigProductionService = stockConfigProductionService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
  }

  @Override
  public String getTrackingNumberDomain(ManufOrder manufOrder, ProdProduct prodProduct)
      throws AxelorException {

    Objects.requireNonNull(prodProduct);

    if (prodProduct.getProduct() == null || manufOrder == null) {
      return "self.id IN (0)";
    }

    var stockConfig = stockConfigProductionService.getStockConfig(manufOrder.getCompany());
    var productionStockLocation =
        stockConfigProductionService.getProductionVirtualStockLocation(
            stockConfig, manufOrderOutsourceService.isOutsource(manufOrder));

    String domain =
        "self.product.id = %d AND"
            + " (self IN (SELECT stockLocationLine.trackingNumber FROM StockLocationLine stockLocationLine WHERE stockLocationLine.detailsStockLocation = %d AND stockLocationLine.currentQty > 0))";
    return String.format(domain, prodProduct.getProduct().getId(), productionStockLocation.getId());
  }
}
