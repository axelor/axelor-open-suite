/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.payment.paymentvoucher;

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherCancelServiceImpl;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCancelService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class PaymentVoucherCancelServiceBankPayImpl extends PaymentVoucherCancelServiceImpl {

  protected BankOrderCancelService bankOrderCancelService;

  @Inject
  public PaymentVoucherCancelServiceBankPayImpl(BankOrderCancelService bankOrderCancelService) {
    this.bankOrderCancelService = bankOrderCancelService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public PaymentVoucher cancelPaymentVoucher(PaymentVoucher paymentVoucher) throws AxelorException {
    BankOrder bankOrder = paymentVoucher.getBankOrder();
    if (bankOrder != null && bankOrder.getStatusSelect() != BankOrderRepository.STATUS_CANCELED) {
      bankOrderCancelService.cancelBankOrder(bankOrder);
    }
    return super.cancelPaymentVoucher(paymentVoucher);
  }
}
