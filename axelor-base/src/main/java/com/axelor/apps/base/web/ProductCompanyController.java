package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.base.service.ProductComputePriceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductCompanyController {

  public void updateSalePrice(ActionRequest request, ActionResponse response) {
    try {
      ProductCompany productCompany = request.getContext().asType(ProductCompany.class);
      if (productCompany.getAutoUpdateSalePrice()) {
        response.setValue(
            "salePrice",
            Beans.get(ProductComputePriceService.class)
                .computeSalePrice(
                    productCompany.getManagPriceCoef(),
                    productCompany.getCostPrice(),
                    productCompany.getProduct(),
                    productCompany.getCompany()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
