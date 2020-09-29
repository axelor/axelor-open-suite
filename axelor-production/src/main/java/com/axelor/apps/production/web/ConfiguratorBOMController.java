package com.axelor.apps.production.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ConfiguratorBOMController {

  public void clearConfigBOMProuct(ActionRequest request, ActionResponse response) {
    ConfiguratorBOM configuratorBOM = request.getContext().asType(ConfiguratorBOM.class);

    Product product = configuratorBOM.getProduct();
    Company company = configuratorBOM.getCompany();
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
