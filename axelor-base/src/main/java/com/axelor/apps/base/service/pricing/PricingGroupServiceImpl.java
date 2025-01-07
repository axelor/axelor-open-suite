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
package com.axelor.apps.base.service.pricing;

import com.axelor.apps.base.db.Pricing;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PricingGroupServiceImpl implements PricingGroupService {

  protected PricingGenericService pricingGenericService;

  @Inject
  public PricingGroupServiceImpl(PricingGenericService pricingGenericService) {
    this.pricingGenericService = pricingGenericService;
  }

  @Override
  public String computeFormulaField(Product product, ProductCategory productCategory) {
    String formula = "";
    if (product == null && productCategory == null) {
      return formula;
    }

    if (product != null) {
      formula =
          String.format(
              "product?.id == %d || product?.parentProduct?.id == %d",
              product.getId(), product.getId());

      if (productCategory != null) {
        formula =
            formula.concat(
                String.format(" || product?.productCategory?.id == %d", productCategory.getId()));
      }
    } else {
      formula = String.format("product?.productCategory?.id == %d", productCategory.getId());
    }
    return formula;
  }

  @Override
  public Map<String, Object> clearFieldsRelatedToFormula(Pricing pricing) {
    Map<String, Object> valuesMap = new HashMap<>();
    valuesMap.put("product", null);
    valuesMap.put("productCategory", null);

    return valuesMap;
  }

  @Override
  public String getConcernedModelDomain(Pricing pricing) {
    String domain = "self.id > 0";

    List<String> unavailableModels = pricingGenericService.getUnavailableModels();

    if (!ObjectUtils.isEmpty(unavailableModels)) {
      String unavailableModelsStr =
          unavailableModels.stream()
              .map(str -> String.format("'%s'", str))
              .collect(Collectors.joining(","));
      domain = String.format("self.name NOT IN (%s)", unavailableModelsStr);
    }

    return domain;
  }
}
