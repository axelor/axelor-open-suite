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
package com.axelor.apps.supplychain.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.rest.StockProductRestServiceImpl;
import com.axelor.apps.supplychain.rest.dto.SupplychainStockProductResponse;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.ResponseConstructor;
import java.util.Map;
import javax.ws.rs.core.Response;

public class StockProductRestServiceSupplychainImpl extends StockProductRestServiceImpl {

  /*
   * OVERRIDING
   * Origin :  StockProductRestController in axelor-stock module
   * Add : stock indicators of supplychain (sale / purchase / production)
   */
  @Override
  public Response getProductIndicators(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    Map<String, Object> stockIndicators;
    if (company == null) {
      stockIndicators =
          Beans.get(ProductStockLocationService.class).computeIndicators(product.getId(), 0L, 0L);
    } else if (stockLocation == null) {
      stockIndicators =
          Beans.get(ProductStockLocationService.class)
              .computeIndicators(product.getId(), company.getId(), 0L);
    } else {
      stockIndicators =
          Beans.get(ProductStockLocationService.class)
              .computeIndicators(product.getId(), company.getId(), stockLocation.getId());
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        "Request completed",
        new SupplychainStockProductResponse(product, stockIndicators));
  }
}
