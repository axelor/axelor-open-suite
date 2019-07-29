package com.axelor.apps.bankpayment.service.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.exception.AxelorException;

public interface InvoicePaymentCreateServiceBankPay {

  public void checkBankOrderAlreadyExist(Invoice invoice) throws AxelorException;
}
