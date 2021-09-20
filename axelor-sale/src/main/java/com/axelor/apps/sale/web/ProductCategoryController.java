package com.axelor.apps.sale.web;

import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.sale.service.ProductCategorySaleService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductCategoryController {

  /**
   * Called from product category view on maxDiscount change. Call {@link
   * ProductCategorySaleService#updateSaleOrderLines(ProductCategory)}.
   *
   * @param request
   * @param response
   */
  public void updateSaleOrderLines(ActionRequest request, ActionResponse response) {
    try {
      ProductCategory productCategory = request.getContext().asType(ProductCategory.class);
      if (productCategory.getId() != null) {
        Beans.get(ProductCategorySaleService.class).updateSaleOrderLines(productCategory);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
