package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.bankpayment.service.InvoiceTermServiceBankPayment;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InvoiceTermController {

  @ErrorException
  public void checkBankDetails(ActionRequest request, ActionResponse response)
      throws AxelorException {
    InvoiceTerm invoiceTerm = request.getContext().asType(InvoiceTerm.class);

    BankDetails bankDetails =
        Beans.get(InvoiceTermServiceBankPayment.class).checkInvoiceTermBankDetails(invoiceTerm);

    response.setValue("bankDetails", bankDetails);
  }
}
