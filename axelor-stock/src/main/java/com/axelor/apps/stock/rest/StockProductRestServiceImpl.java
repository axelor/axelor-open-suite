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
package com.axelor.apps.stock.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductVariant;
import com.axelor.apps.base.db.ProductVariantAttr;
import com.axelor.apps.base.db.ProductVariantValue;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.rest.dto.StockProductResponse;
import com.axelor.apps.stock.rest.dto.StockProductVariantAttributeResponse;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.inject.Beans;
import com.axelor.utils.api.ResponseConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

public class StockProductRestServiceImpl implements StockProductRestService {
  @Override
  public Response getProductIndicators(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    Map<String, Object> stockIndicators;
    if (company == null) {
      stockIndicators =
          Beans.get(StockLocationService.class).getStockIndicators(product.getId(), 0L, 0L);
    } else if (stockLocation == null) {
      stockIndicators =
          Beans.get(StockLocationService.class)
              .getStockIndicators(product.getId(), company.getId(), 0L);
    } else {
      stockIndicators =
          Beans.get(StockLocationService.class)
              .getStockIndicators(product.getId(), company.getId(), stockLocation.getId());
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        "Request completed",
        new StockProductResponse(product, stockIndicators));
  }

  @Override
  public List<StockProductVariantAttributeResponse> fetchAttributes(Product product)
      throws AxelorException {
    List<StockProductVariantAttributeResponse> attributes = new ArrayList<>();

    ProductVariant variant = product.getProductVariant();

    if (variant == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          "Product variant was not found and is required.");
    }

    if (variant.getProductVariantAttr1() != null && variant.getProductVariantValue1() != null) {
      ProductVariantAttr attr1 = variant.getProductVariantAttr1();
      ProductVariantValue value1 = variant.getProductVariantValue1();

      attributes.add(
          new StockProductVariantAttributeResponse(
              attr1.getName(),
              value1.getName(),
              value1.getPriceExtra(),
              value1.getApplicationPriceSelect()));
    }

    if (variant.getProductVariantAttr2() != null && variant.getProductVariantValue2() != null) {
      ProductVariantAttr attr2 = variant.getProductVariantAttr2();
      ProductVariantValue value2 = variant.getProductVariantValue2();

      attributes.add(
          new StockProductVariantAttributeResponse(
              attr2.getName(),
              value2.getName(),
              value2.getPriceExtra(),
              value2.getApplicationPriceSelect()));
    }

    if (variant.getProductVariantAttr3() != null && variant.getProductVariantValue3() != null) {
      ProductVariantAttr attr3 = variant.getProductVariantAttr3();
      ProductVariantValue value3 = variant.getProductVariantValue3();

      attributes.add(
          new StockProductVariantAttributeResponse(
              attr3.getName(),
              value3.getName(),
              value3.getPriceExtra(),
              value3.getApplicationPriceSelect()));
    }

    if (variant.getProductVariantAttr4() != null && variant.getProductVariantValue4() != null) {
      ProductVariantAttr attr4 = variant.getProductVariantAttr4();
      ProductVariantValue value4 = variant.getProductVariantValue4();

      attributes.add(
          new StockProductVariantAttributeResponse(
              attr4.getName(),
              value4.getName(),
              value4.getPriceExtra(),
              value4.getApplicationPriceSelect()));
    }

    if (variant.getProductVariantAttr5() != null && variant.getProductVariantValue5() != null) {
      ProductVariantAttr attr5 = variant.getProductVariantAttr5();
      ProductVariantValue value5 = variant.getProductVariantValue5();

      attributes.add(
          new StockProductVariantAttributeResponse(
              attr5.getName(),
              value5.getName(),
              value5.getPriceExtra(),
              value5.getApplicationPriceSelect()));
    }

    return attributes;
  }
}
