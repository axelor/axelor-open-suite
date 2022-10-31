package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.google.inject.persist.Transactional;

public class PaymentVoucherCancelServiceImpl implements PaymentVoucherCancelService {

  @Override
  @Transactional
  public PaymentVoucher cancelPaymentVoucher(PaymentVoucher paymentVoucher) {

    paymentVoucher.setStatusSelect(PaymentVoucherRepository.STATUS_CANCELED);
    return paymentVoucher;
  }
}
