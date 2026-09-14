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
package com.axelor.apps.account.service.payment.paymentvoucher;

import com.axelor.apps.account.db.DepositSlip;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.service.DepositSlipService;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class PaymentVoucherCancelServiceImpl implements PaymentVoucherCancelService {

  protected DepositSlipService depositSlipService;

  @Inject
  public PaymentVoucherCancelServiceImpl(DepositSlipService depositSlipService) {
    this.depositSlipService = depositSlipService;
  }

  @Override
  @Transactional
  public PaymentVoucher cancelPaymentVoucher(PaymentVoucher paymentVoucher) {
    DepositSlip depositSlip = paymentVoucher.getDepositSlip();
    if (depositSlip != null && !depositSlip.getIsBankDepositMoveGenerated()) {
      depositSlipService.removePaymentVoucher(depositSlip, paymentVoucher);
      depositSlipService.computeTotals(depositSlip);
    }
    paymentVoucher.setStatusSelect(PaymentVoucherRepository.STATUS_CANCELED);
    return paymentVoucher;
  }
}
