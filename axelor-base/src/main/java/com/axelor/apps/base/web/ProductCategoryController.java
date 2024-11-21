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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.service.ProductCategoryDomainCreatorService;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductCategoryController {

  /**
   * Called from product category view on maxDiscount change. Call {@link
   * ProductCategoryService#computeDiscountMessage(ProductCategory)}.
   *
   * @param request
   * @param response
   */
  public void showExistingDiscounts(ActionRequest request, ActionResponse response) {
    try {
      ProductCategory productCategory = request.getContext().asType(ProductCategory.class);
      String discountsMessage =
          Beans.get(ProductCategoryService.class).computeDiscountMessage(productCategory);
      if (!"".equals(discountsMessage)) {
        response.setInfo(discountsMessage);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void filterParentCategory(ActionRequest request, ActionResponse response) {
    try {
      ProductCategory productCategory = request.getContext().asType(ProductCategory.class);
      String domain =
          Beans.get(ProductCategoryDomainCreatorService.class)
              .createProductCategoryDomainFilteringChildren(productCategory);
      response.setAttr("parentProductCategory", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
