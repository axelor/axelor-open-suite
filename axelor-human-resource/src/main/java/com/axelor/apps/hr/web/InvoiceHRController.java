package com.axelor.apps.hr.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.hr.service.InvoiceHRService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class InvoiceHRController {
  public void setBankCardDomain(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    String domain = Beans.get(InvoiceHRService.class).createDomainForBankCard(invoice);
    response.setAttr("bankCard", "domain", domain);
  }
}
