package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCompany;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AccountConfigController {

  public void clearProduct(ActionRequest request, ActionResponse response) throws AxelorException {
    AccountConfig accountConfig = request.getContext().asType(AccountConfig.class);

    Product productAdvancePayment = accountConfig.getAdvancePaymentProduct();
    Product productInvoicing = accountConfig.getInvoicingProduct();

    Company company = accountConfig.getCompany();
    boolean remove = true;

    if (company != null) {
      if (productAdvancePayment != null) {
        for (ProductCompany productCompany : productAdvancePayment.getProductCompanyList()) {
          if (company == productCompany.getCompany()) {
            remove = false;
            break;
          }
        }

        if (remove) {
          response.setValue("advancePaymentProduct", null);
        }
      }

      if (productInvoicing != null) {
        remove = true;
        for (ProductCompany productCompany : productInvoicing.getProductCompanyList()) {
          if (company == productCompany.getCompany()) {
            remove = false;
            break;
          }
        }

        if (remove) {
          response.setValue("invoicingProduct", null);
        }
      }
    }
  }
}
