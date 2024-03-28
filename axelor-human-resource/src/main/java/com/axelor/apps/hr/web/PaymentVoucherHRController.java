package com.axelor.apps.hr.web;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.hr.service.BankCardService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class PaymentVoucherHRController {
  public void setCompanyBankCardDomain(ActionRequest request, ActionResponse response) {
    PaymentVoucher paymentVoucher = request.getContext().asType(PaymentVoucher.class);
    String domain =
        Beans.get(BankCardService.class)
            .createDomainForBankCard(
                paymentVoucher.getCompanyBankDetails(), paymentVoucher.getCompany());
    response.setAttr("companyBankCard", "domain", domain);
  }
}
