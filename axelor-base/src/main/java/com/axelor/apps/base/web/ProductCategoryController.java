package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.exception.service.TraceBackService;
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
        response.setFlash(discountsMessage);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
