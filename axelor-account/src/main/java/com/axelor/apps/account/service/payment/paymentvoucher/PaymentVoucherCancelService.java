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

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.base.AxelorException;

public interface PaymentVoucherCancelService {

  public PaymentVoucher cancelPaymentVoucher(PaymentVoucher paymentVoucher) throws AxelorException;

  /**
   * Checks that the payment voucher can be cancelled by a user-driven move reversal.
   *
   * <p>A voucher included in a validated deposit slip (bank deposit move generated) cannot be
   * cancelled this way: the cheque has already been deposited. Cheque rejection does not use this
   * check, since it cancels a deposited cheque on purpose.
   *
   * @param paymentVoucher the payment voucher about to be cancelled
   * @throws AxelorException if the voucher is included in a validated deposit slip
   */
  void checkDepositSlip(PaymentVoucher paymentVoucher) throws AxelorException;
}
