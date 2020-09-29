package com.axelor.apps.production.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.production.db.UnitCostCalcLine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class UnitCostCalcLineController {

  public void clearUnitCostCalcLineProduct(ActionRequest request, ActionResponse response) {

    UnitCostCalcLine unitCostCalcLine = request.getContext().asType(UnitCostCalcLine.class);

    Product product = unitCostCalcLine.getProduct();
    Company company = unitCostCalcLine.getCompany();
    boolean remove = true;

    if (product != null && company != null) {
      for (ProductCompany productCompany : product.getProductCompanyList()) {
        if (company == productCompany.getCompany()) {
          remove = false;
          break;
        }
      }

      if (remove) {
        response.setValue("product", null);
      }
    }
  }
}
