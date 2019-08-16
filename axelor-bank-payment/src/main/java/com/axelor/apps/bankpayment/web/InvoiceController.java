package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentCreateServiceBankPay;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InvoiceController {

  public void checkBankOrderAlreadyExist(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      invoice = Beans.get(InvoiceRepository.class).find(invoice.getId());
      Beans.get(InvoicePaymentCreateServiceBankPay.class).checkBankOrderAlreadyExist(invoice);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
