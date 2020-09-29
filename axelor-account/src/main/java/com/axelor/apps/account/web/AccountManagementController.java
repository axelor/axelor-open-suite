package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AccountManagementController {

  public void clearAccountManagementProduct(ActionRequest request, ActionResponse response) {

    AccountManagement accountManagement = request.getContext().asType(AccountManagement.class);

    Product product = accountManagement.getProduct();
    Company company = accountManagement.getCompany();
    boolean remove = true;

    if (product != null && company != null) {
      for (ProductCompany productCompany : product.getProductCompanyList()) {
        if (company == productCompany.getCompany()) {
          remove = false;
          break;
        }
      }

      if (remove == true) {
        response.setValue("product", null);
      }
    }
  }
}
