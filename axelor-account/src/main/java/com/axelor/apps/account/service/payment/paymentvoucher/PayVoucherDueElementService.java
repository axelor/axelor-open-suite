package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface PayVoucherDueElementService {

  PayVoucherDueElement updateDueElementWithFinancialDiscount(
      PayVoucherDueElement payVoucherDueElement, PaymentVoucher paymentVoucher)
      throws AxelorException;

  BigDecimal calculateFinancialDiscountTaxAmount(PayVoucherDueElement payVoucherDueElement)
      throws AxelorException;

  BigDecimal calculateFinancialDiscountTotalAmount(PayVoucherDueElement payVoucherDueElement)
      throws AxelorException;

  PayVoucherDueElement updateAmounts(PayVoucherDueElement payVoucherDueElement)
      throws AxelorException;
}
