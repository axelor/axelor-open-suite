package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.bankpayment.service.InvoiceBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InvoiceController {

  @ErrorException
  public void cancelLcr(ActionRequest request, ActionResponse response) throws AxelorException {
    Invoice invoice = request.getContext().asType(Invoice.class);

    Beans.get(InvoiceBankPaymentService.class).cancelLcr(invoice);

    response.setReload(true);
  }
}
