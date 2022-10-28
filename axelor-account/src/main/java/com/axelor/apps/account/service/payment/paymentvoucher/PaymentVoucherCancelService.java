package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.PaymentVoucher;
import com.google.inject.persist.Transactional;

public interface PaymentVoucherCancelService {

  @Transactional
  public PaymentVoucher cancelPaymentVoucher(PaymentVoucher paymentVoucher);
}
