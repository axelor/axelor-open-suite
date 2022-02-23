package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.exception.AxelorException;

public interface PayVoucherDueElementService {

  PayVoucherDueElement updateDueElementWithFinancialDiscount(
      PayVoucherDueElement payVoucherDueElement, PaymentVoucher paymentVoucher)
      throws AxelorException;

  PayVoucherDueElement updateAmounts(PayVoucherDueElement payVoucherDueElement)
      throws AxelorException;
}
