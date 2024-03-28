package com.axelor.apps.hr.web;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.hr.service.BankCardService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class InvoicePaymentHRController {
  public void setCompanyBankCardDomain(ActionRequest request, ActionResponse response) {
    InvoicePayment invoicePayment = request.getContext().asType(InvoicePayment.class);
    String domain =
        Beans.get(BankCardService.class)
            .createDomainForBankCard(
                invoicePayment.getCompanyBankDetails(), invoicePayment.getInvoice().getCompany());
    response.setAttr("companyBankCard", "domain", domain);
  }
}
