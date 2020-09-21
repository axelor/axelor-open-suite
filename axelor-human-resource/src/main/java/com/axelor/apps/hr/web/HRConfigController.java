package com.axelor.apps.hr.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class HRConfigController {

  public void clearHRConfigProducts(ActionRequest request, ActionResponse response) {

    HRConfig hrConfig = request.getContext().asType(HRConfig.class);
    Product uniqueTimesheetProduct = hrConfig.getUniqueTimesheetProduct();
    Product kilometricExpenseProduct = hrConfig.getKilometricExpenseProduct();
    Company hrConfigCompany = hrConfig.getCompany();
    Boolean remove = true;

    if (uniqueTimesheetProduct != null && hrConfigCompany != null) {
      for (ProductCompany productCompany : uniqueTimesheetProduct.getProductCompanyList()) {
        if (productCompany.getCompany() == hrConfigCompany) {
          remove = false;
          break;
        }
      }

      if (remove) {
        response.setValue("uniqueTimesheetProduct", null);
      }
    }

    if (kilometricExpenseProduct != null && hrConfigCompany != null) {
      remove = true;
      for (ProductCompany productCompany : kilometricExpenseProduct.getProductCompanyList()) {
        if (productCompany.getCompany() == hrConfigCompany) {
          remove = false;
          break;
        }
      }

      if (remove) {
        response.setValue("kilometricExpenseProduct", null);
      }
    }
  }
}
